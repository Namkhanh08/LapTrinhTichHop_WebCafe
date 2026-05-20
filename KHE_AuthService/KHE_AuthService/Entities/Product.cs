using System.ComponentModel.DataAnnotations.Schema;

namespace KHE_AuthService.Entities
{
    public class Product
    {
        public int Id { get; set; }

        public string Name { get; set; } = null!;

        public decimal Price { get; set; }
        public int Stock { get; set; }

        public string ImageUrl { get; set; } = null!;


        public string Description { get; set; } = null!;

        public int CategoryId { get; set; }

        public virtual ICollection<CartItem> CartItems { get; set; } = new List<CartItem>();

        public virtual Category Category { get; set; } = null!;

        public virtual ICollection<InventoryLog> InventoryLogs { get; set; } = new List<InventoryLog>();

        public virtual ICollection<OrderDetail> OrderDetails { get; set; } = new List<OrderDetail>();

        public virtual ICollection<ProductDetail> ProductDetails { get; set; } = new List<ProductDetail>();

        public virtual ICollection<RoastingBatch> RoastingBatches { get; set; } = new List<RoastingBatch>();

        [ForeignKey("ProductId")]
        [InverseProperty("Products")]
        public virtual ICollection<GrindingOption> GrindingOptions { get; set; } = new List<GrindingOption>();
    }
}
