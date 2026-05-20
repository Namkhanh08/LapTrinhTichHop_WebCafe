using KHE_AuthService.Data;
using KHE_AuthService.Entities;
using Microsoft.EntityFrameworkCore;
namespace KHE_AuthService.Repositories
{
    public class CategoryRepository : ICategoryRepository
    {
        private readonly AppDbContext _context;         
        public CategoryRepository(AppDbContext context)
        {
            _context = context;
        }
        
        public async Task<IEnumerable<Category>> GetAllCategoriesAsync()
        {
            return await _context.Categories.OrderBy(c => c.Name).ToListAsync();
        }
    }
}
