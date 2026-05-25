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
            await db.Database.ExecuteSqlRawAsync("""
                CREATE TABLE IF NOT EXISTS warehouses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    code VARCHAR(50) NOT NULL UNIQUE,
                    name VARCHAR(255) NOT NULL,
                    location VARCHAR(255),
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

                CREATE TABLE IF NOT EXISTS raw_material_types (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL UNIQUE,
                    description TEXT,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

                CREATE TABLE IF NOT EXISTS suppliers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL UNIQUE,
                    contact_name VARCHAR(255),
                    phone VARCHAR(20),
                    email VARCHAR(255),
                    address TEXT,
                    is_active BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

                CREATE TABLE IF NOT EXISTS raw_material_lots (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    raw_material_type_id INT NULL,
                    bean_type VARCHAR(100) NOT NULL,
                    supplier_id INT NULL,
                    supplier VARCHAR(255) NOT NULL,
                    quantity_kg DECIMAL(10, 2) NOT NULL,
                    quantity_remaining_kg DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                    received_date DATE NOT NULL,
                    expiration_date DATE NOT NULL,
                    origin_region VARCHAR(100),
                    notes TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

                ALTER TABLE inventory_items
                    ADD COLUMN IF NOT EXISTS warehouse_id INT NULL AFTER quantity_reserved;

                ALTER TABLE raw_material_lots
                    ADD COLUMN IF NOT EXISTS raw_material_type_id INT NULL AFTER id,
                    ADD COLUMN IF NOT EXISTS supplier_id INT NULL AFTER bean_type,
                    ADD COLUMN IF NOT EXISTS quantity_remaining_kg DECIMAL(10, 2) NOT NULL DEFAULT 0.00 AFTER quantity_kg;

                UPDATE raw_material_lots
                SET quantity_remaining_kg = quantity_kg
                WHERE quantity_remaining_kg = 0;
                """);
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

