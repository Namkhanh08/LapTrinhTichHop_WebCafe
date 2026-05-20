using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace KHE_AuthService.Entities
{
    public class Cart
    {
        public int Id { get; set; }

     
        public string UserId { get; set; } = null!;


        public virtual ICollection<CartItem> CartItems { get; set; } = new List<CartItem>();

        public virtual User User { get; set; } = null!;
    }
}
