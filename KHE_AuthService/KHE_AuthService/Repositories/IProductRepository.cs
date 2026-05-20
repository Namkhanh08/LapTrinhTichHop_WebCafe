using KHE_AuthService.DTOs;
using KHE_AuthService.Entities;

namespace KHE_AuthService.Repositories
{
    public interface IProductRepository
    {
        Task<IEnumerable<object>> GetAllProductsAsync();
        Task<Product?> GetProductByIdAsync(int id);
        Task<bool> AddProductAsync(ProductRequest request);
        Task<bool> UpdateProductAsync(ProductRequest request);
        Task<bool> DeleteProductAsync(int id);

      
    }
}