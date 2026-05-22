using System.ComponentModel.DataAnnotations;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection")
    ?? "Server=mysql;Port=3306;Database=revo_identity;User=root;Password=root;";

builder.Services.AddDbContext<IdentityDbContext>(options =>
    options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 46))));
builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
});
builder.Services.AddCors(options =>
{
    options.AddDefaultPolicy(policy => policy.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod());
});

var app = builder.Build();
app.UseCors();

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<IdentityDbContext>();
    for (var attempt = 1; ; attempt++)
    {
        try
        {
            db.Database.EnsureCreated();
            break;
        }
        catch when (attempt < 12)
        {
            await Task.Delay(TimeSpan.FromSeconds(5));
        }
    }

    await SeedAdminUser(db);
}

app.MapGet("/health", () => Results.Ok(new { status = "ok", service = "identity-service-dotnet" }));

app.MapPost("/api/auth/register", async (RegisterRequest request, IdentityDbContext db) =>
{
    var errors = ValidateRegister(request);
    if (errors.Count > 0)
    {
        return Results.BadRequest(new { errors });
    }

    var email = request.Email.Trim().ToLowerInvariant();
    if (await db.Users.AnyAsync(u => u.Email == email))
    {
        return Results.Conflict(new { error = "Email already registered" });
    }

    var user = new User
    {
        FullName = request.FullName.Trim(),
        Email = email,
        PasswordHash = Passwords.Hash(request.Password),
        Role = UserRole.customer,
        LoyaltyPoints = 0,
        CreatedAt = DateTime.UtcNow
    };

    db.Users.Add(user);
    await db.SaveChangesAsync();

    return Results.Created($"/api/auth/users/{user.Id}", new
    {
        message = "Registration successful",
        user = PublicUser.From(user)
    });
});

app.MapPost("/api/auth/login", async (LoginRequest request, IdentityDbContext db, IConfiguration configuration) =>
{
    var email = NormalizeLoginIdentifier(request.Email ?? request.Username ?? string.Empty);
    var user = await db.Users.FirstOrDefaultAsync(u => u.Email == email);

    if (user is null || !Passwords.Verify(request.Password, user.PasswordHash))
    {
        return Results.Unauthorized();
    }

    var accessToken = Tokens.Create(user, configuration, TimeSpan.FromHours(2));
    var refreshToken = Tokens.Create(user, configuration, TimeSpan.FromDays(14));

    return Results.Ok(new
    {
        message = "Login successful",
        token = accessToken,
        access_token = accessToken,
        refresh_token = refreshToken,
        token_type = "Bearer",
        userId = user.Id,
        userName = user.Email,
        name = user.FullName,
        position = user.Role == UserRole.admin ? "Admin" : "User",
        phone = user.Phone,
        user = PublicUser.From(user)
    });
});

app.MapGet("/api/auth/profile", async (HttpContext context, IdentityDbContext db, IConfiguration configuration) =>
{
    var userId = Tokens.ReadUserId(context, configuration);
    if (userId is null)
    {
        return Results.Unauthorized();
    }

    var user = await db.Users.FindAsync(userId.Value);
    return user is null
        ? Results.NotFound(new { error = "User not found" })
        : Results.Ok(new { user = PublicUser.From(user) });
});

app.MapGet("/api/auth/users", async (HttpContext context, IdentityDbContext db, IConfiguration configuration) =>
{
    var role = Tokens.ReadRole(context, configuration);
    if (role != UserRole.admin.ToString())
    {
        return Results.Forbid();
    }

    var users = await db.Users
        .OrderByDescending(u => u.CreatedAt)
        .Select(u => PublicUser.From(u))
        .ToListAsync();

    return Results.Ok(new { items = users, total = users.Count });
});

app.Run();

static List<string> ValidateRegister(RegisterRequest request)
{
    var errors = new List<string>();
    if (string.IsNullOrWhiteSpace(request.FullName))
    {
        errors.Add("Full name is required");
    }
    if (string.IsNullOrWhiteSpace(request.Email) || !new EmailAddressAttribute().IsValid(request.Email))
    {
        errors.Add("Valid email is required");
    }
    if (string.IsNullOrWhiteSpace(request.Password)
        || request.Password.Length < 8
        || !request.Password.Any(char.IsUpper)
        || !request.Password.Any(char.IsLower)
        || !request.Password.Any(char.IsDigit)
        || !request.Password.Any(ch => !char.IsLetterOrDigit(ch)))
    {
        errors.Add("Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
    }
    return errors;
}

static string NormalizeLoginIdentifier(string value)
{
    var normalized = value.Trim().ToLowerInvariant();
    return normalized switch
    {
        "admin" => "admin@revo.coffee",
        "user01" => "user01@gmail.com",
        "user02" => "user02@gmail.com",
        _ => normalized
    };
}

static async Task SeedAdminUser(IdentityDbContext db)
{
    const string adminEmail = "admin@revo.coffee";
    const string adminPassword = "@admi123";

    var admin = await db.Users.FirstOrDefaultAsync(u => u.Email == adminEmail);
    if (admin is null)
    {
        db.Users.Add(new User
        {
            FullName = "Admin Revo",
            Email = adminEmail,
            PasswordHash = Passwords.Hash(adminPassword),
            Role = UserRole.admin,
            LoyaltyPoints = 0,
            CreatedAt = DateTime.UtcNow
        });
    }
    else
    {
        admin.FullName = string.IsNullOrWhiteSpace(admin.FullName) ? "Admin Revo" : admin.FullName;
        admin.PasswordHash = Passwords.Hash(adminPassword);
        admin.Role = UserRole.admin;
    }

    await db.SaveChangesAsync();
}

public sealed record RegisterRequest(
    [property: JsonPropertyName("name")] string FullName,
    string Email,
    string Password);

public sealed record LoginRequest(string? Email, string? Username, string Password);

public sealed record PublicUser(int Id, string FullName, string Name, string Email, UserRole Role, int LoyaltyPoints, string? Phone)
{
    public static PublicUser From(User user) => new(user.Id, user.FullName, user.FullName, user.Email, user.Role, user.LoyaltyPoints, user.Phone);
}

public enum UserRole
{
    admin,
    customer
}

public sealed class User
{
    public int Id { get; set; }
    [MaxLength(255)]
    public string FullName { get; set; } = string.Empty;
    [MaxLength(255)]
    public string Email { get; set; } = string.Empty;
    [MaxLength(255)]
    public string PasswordHash { get; set; } = string.Empty;
    public UserRole Role { get; set; } = UserRole.customer;
    public int LoyaltyPoints { get; set; }
    [MaxLength(20)]
    public string? Phone { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}

public sealed class IdentityDbContext : DbContext
{
    public IdentityDbContext(DbContextOptions<IdentityDbContext> options) : base(options) { }
    public DbSet<User> Users => Set<User>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(entity =>
        {
            entity.ToTable("users");
            entity.HasKey(u => u.Id);
            entity.Property(u => u.Id).HasColumnName("id");
            entity.Property(u => u.FullName).HasColumnName("full_name").HasMaxLength(255).IsRequired();
            entity.Property(u => u.Email).HasColumnName("email").HasMaxLength(255).IsRequired();
            entity.HasIndex(u => u.Email).IsUnique();
            entity.Property(u => u.PasswordHash).HasColumnName("password_hash").HasMaxLength(255).IsRequired();
            entity.Property(u => u.Role).HasColumnName("role").HasConversion<string>().HasDefaultValue(UserRole.customer);
            entity.Property(u => u.LoyaltyPoints).HasColumnName("loyalty_points").HasDefaultValue(0);
            entity.Property(u => u.Phone).HasColumnName("phone").HasMaxLength(20);
            entity.Property(u => u.CreatedAt).HasColumnName("created_at");
        });
    }
}

public static class Passwords
{
    public static string Hash(string password)
    {
        var salt = RandomNumberGenerator.GetBytes(16);
        var hash = Rfc2898DeriveBytes.Pbkdf2(password, salt, 100_000, HashAlgorithmName.SHA256, 32);
        return $"pbkdf2${Convert.ToBase64String(salt)}${Convert.ToBase64String(hash)}";
    }

    public static bool Verify(string password, string stored)
    {
        if (stored.StartsWith("$2y$", StringComparison.Ordinal))
        {
            return false; // bcrypt hashes are not supported in this implementation
        }

        var parts = stored.Split('$');
        if (parts.Length != 3 || parts[0] != "pbkdf2")
        {
            return false;
        }

        var salt = Convert.FromBase64String(parts[1]);
        var expected = Convert.FromBase64String(parts[2]);
        var actual = Rfc2898DeriveBytes.Pbkdf2(password, salt, 100_000, HashAlgorithmName.SHA256, 32);
        return CryptographicOperations.FixedTimeEquals(actual, expected);
    }
}

public static class Tokens
{
    public static string Create(User user, IConfiguration configuration, TimeSpan lifetime)
    {
        var secret = configuration["Jwt:Secret"] ?? Environment.GetEnvironmentVariable("JWT_SECRET") ?? "revo-dev-secret-change-before-production";
        var header = Base64Url(JsonSerializer.SerializeToUtf8Bytes(new { alg = "HS256", typ = "JWT" }));
        var payload = Base64Url(JsonSerializer.SerializeToUtf8Bytes(new Dictionary<string, object>
        {
            ["sub"] = user.Id.ToString(),
            ["name"] = user.FullName,
            ["email"] = user.Email,
            ["role"] = user.Role.ToString(),
            ["exp"] = DateTimeOffset.UtcNow.Add(lifetime).ToUnixTimeSeconds()
        }));
        var unsigned = $"{header}.{payload}";
        var signature = Sign(unsigned, secret);
        return $"{unsigned}.{signature}";
    }

    public static int? ReadUserId(HttpContext context, IConfiguration configuration)
    {
        var payload = ReadPayload(context, configuration);
        return payload is not null && payload.TryGetValue("sub", out var sub) && int.TryParse(sub?.ToString(), out var id)
            ? id
            : null;
    }

    public static string? ReadRole(HttpContext context, IConfiguration configuration)
    {
        var payload = ReadPayload(context, configuration);
        return payload is not null && payload.TryGetValue("role", out var role) ? role?.ToString() : null;
    }

    private static Dictionary<string, object>? ReadPayload(HttpContext context, IConfiguration configuration)
    {
        var auth = context.Request.Headers.Authorization.ToString();
        if (!auth.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase))
        {
            return null;
        }

        var secret = configuration["Jwt:Secret"] ?? Environment.GetEnvironmentVariable("JWT_SECRET") ?? "revo-dev-secret-change-before-production";
        var token = auth["Bearer ".Length..];
        var parts = token.Split('.');
        if (parts.Length != 3)
        {
            return null;
        }

        var unsigned = $"{parts[0]}.{parts[1]}";
        if (!CryptographicOperations.FixedTimeEquals(Encoding.UTF8.GetBytes(Sign(unsigned, secret)), Encoding.UTF8.GetBytes(parts[2])))
        {
            return null;
        }

        try
        {
            var payload = JsonSerializer.Deserialize<Dictionary<string, object>>(Base64UrlDecode(parts[1]));
            if (payload is null || !payload.TryGetValue("exp", out var expValue))
            {
                return null;
            }

            var exp = Convert.ToInt64(expValue.ToString());
            return DateTimeOffset.UtcNow.ToUnixTimeSeconds() > exp ? null : payload;
        }
        catch
        {
            return null;
        }
    }

    private static string Sign(string value, string secret)
    {
        using var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret));
        return Base64Url(hmac.ComputeHash(Encoding.UTF8.GetBytes(value)));
    }

    private static string Base64Url(byte[] bytes)
    {
        return Convert.ToBase64String(bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_');
    }

    private static byte[] Base64UrlDecode(string value)
    {
        var padded = value.Replace('-', '+').Replace('_', '/');
        padded = padded.PadRight(padded.Length + (4 - padded.Length % 4) % 4, '=');
        return Convert.FromBase64String(padded);
    }
}
