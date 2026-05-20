using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace KHE_AuthService.Entities
{
    public class Order
    {
        public int Id { get; set; }

        public string UserId { get; set; } = null!;

        public DateTime OrderDate { get; set; }

        public decimal TotalAmount { get; set; }

        public string Status { get; set; } = null!;

     
        public string? ReceiverName { get; set; }

        public string? ReceiverPhone { get; set; }
        public string? ReceiverEmail { get; set; }

      
        public string? ShippingProvince { get; set; }

        public string? ShippingDistrict { get; set; }

        public string? ShippingWard { get; set; }

        public string? ShippingDetailAddress { get; set; }

        public string? ShippingNote { get; set; }

        public string? PaymentMethod { get; set; }

        public virtual ICollection<OrderDetail> OrderDetails { get; set; } = new List<OrderDetail>();

        public virtual User User { get; set; } = null!;
    }
}
