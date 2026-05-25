using Yarp.ReverseProxy.Configuration;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.IdentityModel.Tokens;
using System.Text;
using System.Security.Claims;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
        policy.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod());
});

builder.Services.AddReverseProxy()
    .LoadFromMemory(CreateRoutes(), CreateClusters());

var app = builder.Build();

app.UseCors();

app.Use(async (context, next) =>
{
    RemoveForwardedUserHeaders(context);

    var authHeader = context.Request.Headers["Authorization"].ToString();
    if (!string.IsNullOrEmpty(authHeader) && authHeader.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
    {
        var token = authHeader.Substring(7).Trim();
        var handler = new JwtSecurityTokenHandler();
        
        try
        {
            var secret = Environment.GetEnvironmentVariable("JWT_SECRET") ?? "revo-dev-secret-change-before-production";
            var key = Encoding.UTF8.GetBytes(secret);
            
            var validationParameters = new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(key),
                ValidateIssuer = false,
                ValidateAudience = false,
                ValidateLifetime = true,
                ClockSkew = TimeSpan.Zero
            };

            var principal = handler.ValidateToken(token, validationParameters, out var validatedToken);
            
            var userId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value 
                         ?? principal.FindFirst("sub")?.Value 
                         ?? principal.FindFirst("nameid")?.Value;
            var role = principal.FindFirst(ClaimTypes.Role)?.Value 
                       ?? principal.FindFirst("role")?.Value;
            var email = principal.FindFirst(ClaimTypes.Email)?.Value 
                        ?? principal.FindFirst("email")?.Value;

            if (!string.IsNullOrEmpty(userId))
            {
                context.Request.Headers["X-User-Id"] = userId;
            }
            if (!string.IsNullOrEmpty(role))
            {
                context.Request.Headers["X-User-Role"] = role;
            }
            if (!string.IsNullOrEmpty(email))
            {
                context.Request.Headers["X-User-Email"] = email;
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"JWT verification failed: {ex.Message}");
        }
    }

    await next();
});

app.Use(async (context, next) =>
{
    const string correlationHeader = "X-Correlation-ID";

    if (!context.Request.Headers.TryGetValue(correlationHeader, out var correlationId)
        || string.IsNullOrWhiteSpace(correlationId))
    {
        correlationId = Guid.NewGuid().ToString("N");
        context.Request.Headers[correlationHeader] = correlationId;
    }

    context.Response.OnStarting(() =>
    {
        context.Response.Headers[correlationHeader] = correlationId.ToString();
        return Task.CompletedTask;
    });

    await next();
});

app.MapGet("/health", () => Results.Ok(new { status = "up", service = "api-gateway-dotnet" }));
app.MapReverseProxy();

app.Run();

static IReadOnlyList<RouteConfig> CreateRoutes()
{
    var routes = new List<RouteConfig>();

    AddRoutePair(routes, "identity-auth", "/api/auth", "identity");
    AddRoutePair(routes, "products", "/api/products", "products");
    AddRoutePair(routes, "categories", "/api/categories", "products");
    AddRoutePair(routes, "inventory", "/api/inventory", "inventory");
    AddRoutePair(routes, "stock-movements", "/api/stockmovements", "inventory");
    AddRoutePair(routes, "raw-material-lots", "/api/rawmateriallots", "inventory");
    AddRoutePair(routes, "orders", "/api/orders", "orders");
    AddRoutePair(routes, "carts", "/api/carts", "orders");
    AddRoutePair(routes, "subscriptions", "/api/subscriptions", "orders");
    AddRoutePair(routes, "vouchers", "/api/vouchers", "orders");
    AddRoutePair(routes, "batches", "/api/batches", "batches");
    AddRoutePair(routes, "dashboard", "/api/dashboard", "orders");

    return routes;
}

static IReadOnlyList<ClusterConfig> CreateClusters()
{
    return
    [
        CreateCluster("identity", GetServiceUrl("IDENTITY_SERVICE_URL", "http://identity-service:80")),
        CreateCluster("products", GetServiceUrl("PRODUCT_SERVICE_URL", "http://product-service:80")),
        CreateCluster("inventory", GetServiceUrl("INVENTORY_SERVICE_URL", "http://inventory-service:80")),
        CreateCluster("orders", GetServiceUrl("ORDER_SERVICE_URL", "http://order-service:8080")),
        CreateCluster("batches", GetServiceUrl("BATCH_SERVICE_URL", "http://batch-service:8080"))
    ];
}

static RouteConfig CreateRoute(string routeId, string path, string clusterId)
{
    return new RouteConfig
    {
        RouteId = routeId,
        ClusterId = clusterId,
        Match = new RouteMatch
        {
            Path = path
        }
    };
}

static void AddRoutePair(List<RouteConfig> routes, string routeId, string pathPrefix, string clusterId)
{
    routes.Add(CreateRoute(routeId, pathPrefix, clusterId));
    routes.Add(CreateRoute($"{routeId}-catch-all", $"{pathPrefix}/{{**catch-all}}", clusterId));
}

static ClusterConfig CreateCluster(string clusterId, string address)
{
    return new ClusterConfig
    {
        ClusterId = clusterId,
        Destinations = new Dictionary<string, DestinationConfig>
        {
            [clusterId] = new()
            {
                Address = EnsureTrailingSlash(address)
            }
        }
    };
}

static string GetServiceUrl(string environmentVariable, string fallback)
{
    return Environment.GetEnvironmentVariable(environmentVariable) ?? fallback;
}

static string EnsureTrailingSlash(string value)
{
    return value.EndsWith('/') ? value : $"{value}/";
}

static void RemoveForwardedUserHeaders(HttpContext context)
{
    context.Request.Headers.Remove("X-User-Id");
    context.Request.Headers.Remove("X-User-Role");
    context.Request.Headers.Remove("X-User-Email");
}
