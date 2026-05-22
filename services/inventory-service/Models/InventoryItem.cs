using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace InventoryService.Models
{
    public class InventoryItem
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Required]
        public string ProductId { get; set; } = string.Empty;

        public string ProductName { get; set; } = string.Empty;

        public int QuantityAvailable { get; set; } = 0;

        public int QuantityReserved { get; set; } = 0;

        public string WarehouseLocation { get; set; } = string.Empty;

        public int ReorderLevel { get; set; } = 20;

        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}

