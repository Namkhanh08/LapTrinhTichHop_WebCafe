using Microsoft.EntityFrameworkCore;
using KHE_AuthService.Entities;

namespace KHE_AuthService.Data
{
    public class AppDbContext : DbContext
    {
        public DbSet<User> Users { get; set; }

        public AppDbContext(DbContextOptions<AppDbContext> options) : base (options) { }
        public DbSet<Cart> Carts { get; set; }

        public DbSet<CartItem> CartItems { get; set; }

        public DbSet<Category> Categories { get; set; }

        public DbSet<GrindingOption> GrindingOptions { get; set; }

        public DbSet<Order> Orders { get; set; }

        public DbSet<OrderDetail> OrderDetails { get; set; }

        public DbSet<Product> Products { get; set; }

        public DbSet<ProductDetail> ProductDetails { get; set; }
        public virtual DbSet<RawMaterial> RawMaterials { get; set; }
        public virtual DbSet<InventoryReceipt> InventoryReceipts { get; set; }
        public DbSet<RawMaterialLog> RawMaterialLogs { get; set; }
        public DbSet<RoastingBatch> RoastingBatches { get; set; }
    }
}
