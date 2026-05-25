using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace InventoryService.Models
{
    public class RawMaterialLot
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        public int? RawMaterialTypeId { get; set; }

        [Required]
        public string BeanType { get; set; } = string.Empty;

        public int? SupplierId { get; set; }

        [Required]
        public string Supplier { get; set; } = string.Empty;

        public decimal QuantityKg { get; set; }

        public decimal QuantityRemainingKg { get; set; }

        public DateTime ReceivedDate { get; set; } = DateTime.UtcNow.Date;

        public DateTime ExpirationDate { get; set; } = DateTime.UtcNow.Date.AddMonths(6);

        public string? OriginRegion { get; set; }

        public string? Notes { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }
}
