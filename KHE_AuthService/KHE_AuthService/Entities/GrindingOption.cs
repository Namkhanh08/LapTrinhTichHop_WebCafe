using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace KHE_AuthService.Entities
{
    public class GrindingOption
    {
        public int Id { get; set; }

        public string Name { get; set; } = null!;

        public virtual ICollection<CartItem> CartItems { get; set; } = new List<CartItem>();

        public virtual ICollection<Product> Products { get; set; } = new List<Product>();
    }
}
