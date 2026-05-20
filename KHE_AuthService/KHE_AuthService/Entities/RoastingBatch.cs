using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace KHE_AuthService.Entities
{
    public class RoastingBatch
    {
        public int Id { get; set; }

        public string BatchCode { get; set; } = null!;

        public int ProductId { get; set; }

      
        public string? RoastLevel { get; set; }

        public double? InputWeight { get; set; }

        public string? Status { get; set; }

        public DateTime? RoastDate { get; set; }

        public string? UserId { get; set; }

    
        public virtual Product Product { get; set; } = null!;

        public virtual User? User { get; set; }
    }
}
