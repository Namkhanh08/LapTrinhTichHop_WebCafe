using Microsoft.EntityFrameworkCore;
using InventoryService.Data;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new() { Title = "Revo Coffee - Inventory Service", Version = "v1" });
});

// Database context
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? "Server=mysql;Port=3306;Database=revo_inventory;User=root;Password=root;";

builder.Services.AddDbContext<InventoryDbContext>(options =>
    options.UseMySql(connectionString, new MySqlServerVersion(new Version(8, 0, 46))));

var app = builder.Build();

// Auto-create database tables
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<InventoryDbContext>();
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
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "Inventory Service v1"));
}

app.MapControllers();
app.MapGet("/health", () => Results.Ok(new { status = "ok", service = "inventory-service" }));

app.Run();

