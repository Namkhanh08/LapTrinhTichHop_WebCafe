using KHE_AuthService.DTOs;

namespace KHE_AuthService.Services
{
    public interface IAuthService
    {
        Task<ApiResponse<string>> RegisterAsync(RegisterRequest request);
        Task<ApiResponse<object>> LoginAsync(LoginRequest request);
    }
}
