using KHE_AuthService.Entities;
namespace KHE_AuthService.Repositories
{
    public interface IUserRepository
    {
        Task<IEnumerable<User>> GetAllAsync();
        Task<User> GetByIdAsync(string id);
        Task<bool> UpdateAsync(User user, bool isAdmin);
        Task<bool> DeleteAsync(string id);
        Task<bool> CreateAsync(User user);
    }
}
