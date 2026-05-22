using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace InventoryService.Models
{
    public enum MovementType
    {
        In,
        Out,
        Reserve,
        Release,
        Adjustment
    }

    public class StockMovement
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }

        [Required]
        public string ProductId { get; set; } = string.Empty;

        public int Quantity { get; set; }

        public MovementType MovementType { get; set; }

        public string? ReferenceId { get; set; }

        public string? ReferenceType { get; set; }

        public string? Notes { get; set; }

        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}

