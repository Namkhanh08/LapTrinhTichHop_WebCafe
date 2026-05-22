using Microsoft.EntityFrameworkCore;
using InventoryService.Models;

namespace InventoryService.Data
{
    public class InventoryDbContext : DbContext
    {
        public InventoryDbContext(DbContextOptions<InventoryDbContext> options) : base(options) { }

        public DbSet<InventoryItem> InventoryItems { get; set; }
        public DbSet<StockMovement> StockMovements { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<InventoryItem>(entity =>
            {
                entity.ToTable("inventory_items");
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ProductId).HasColumnName("product_id");
                entity.Property(e => e.ProductName).HasColumnName("product_name");
                entity.Property(e => e.QuantityAvailable).HasColumnName("quantity_available");
                entity.Property(e => e.QuantityReserved).HasColumnName("quantity_reserved");
                entity.Property(e => e.WarehouseLocation).HasColumnName("warehouse_location");
                entity.Property(e => e.ReorderLevel).HasColumnName("reorder_level");
                entity.Property(e => e.UpdatedAt).HasColumnName("updated_at");
                entity.Property(e => e.CreatedAt).HasColumnName("created_at");
                entity.HasIndex(e => e.ProductId).IsUnique();
            });

            modelBuilder.Entity<StockMovement>(entity =>
            {
                entity.ToTable("stock_movements");
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.ProductId).HasColumnName("product_id");
                entity.Property(e => e.Quantity).HasColumnName("quantity");
                entity.Property(e => e.MovementType)
                    .HasColumnName("movement_type")
                    .HasConversion(
                        value => value.ToString().ToLowerInvariant(),
                        value => Enum.Parse<MovementType>(value, true));
                entity.Property(e => e.ReferenceId).HasColumnName("reference_id");
                entity.Property(e => e.ReferenceType).HasColumnName("reference_type");
                entity.Property(e => e.Notes).HasColumnName("notes");
                entity.Property(e => e.CreatedAt).HasColumnName("created_at");
            });
        }
    }
}

