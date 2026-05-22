using Yarp.ReverseProxy.Configuration;

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
