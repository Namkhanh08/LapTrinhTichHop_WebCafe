using KHE_AuthService.Entities;

namespace KHE_AuthService.Repositories
{
    public interface ICategoryRepository
    {
        Task<IEnumerable<Category>> GetAllCategoriesAsync();
    }
}
