using System;

namespace KHE_AuthService.Dtos
{
    public class ImportMaterialRequest
    {
        public int RawMaterialId { get; set; }
        public string Supplier { get; set; }
        public double Quantity { get; set; }
        public DateTime ImportDate { get; set; }
        public DateTime ExpiryDate { get; set; }
    }
}