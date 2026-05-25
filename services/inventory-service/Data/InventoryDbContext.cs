using Microsoft.EntityFrameworkCore;
using InventoryService.Models;

namespace InventoryService.Data
{
    public class InventoryDbContext : DbContext
    {
        public InventoryDbContext(DbContextOptions<InventoryDbContext> options) : base(options) { }

        public DbSet<InventoryItem> InventoryItems { get; set; }
        public DbSet<StockMovement> StockMovements { get; set; }
        public DbSet<RawMaterialLot> RawMaterialLots { get; set; }

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
                entity.Property(e => e.WarehouseId).HasColumnName("warehouse_id");
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

            modelBuilder.Entity<RawMaterialLot>(entity =>
            {
                entity.ToTable("raw_material_lots");
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.RawMaterialTypeId).HasColumnName("raw_material_type_id");
                entity.Property(e => e.BeanType).HasColumnName("bean_type").HasMaxLength(100).IsRequired();
                entity.Property(e => e.SupplierId).HasColumnName("supplier_id");
                entity.Property(e => e.Supplier).HasColumnName("supplier").HasMaxLength(255).IsRequired();
                entity.Property(e => e.QuantityKg).HasColumnName("quantity_kg").HasPrecision(10, 2);
                entity.Property(e => e.QuantityRemainingKg).HasColumnName("quantity_remaining_kg").HasPrecision(10, 2);
                entity.Property(e => e.ReceivedDate).HasColumnName("received_date");
                entity.Property(e => e.ExpirationDate).HasColumnName("expiration_date");
                entity.Property(e => e.OriginRegion).HasColumnName("origin_region").HasMaxLength(100);
                entity.Property(e => e.Notes).HasColumnName("notes");
                entity.Property(e => e.CreatedAt).HasColumnName("created_at");
                entity.Property(e => e.UpdatedAt).HasColumnName("updated_at");
            });
        }
    }
}

