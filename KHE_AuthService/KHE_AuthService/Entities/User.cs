using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;
namespace KHE_AuthService.Entities
{
    public class User
    {
        public string Id { get; set; } = null!;


        public string Name { get; set; } = null!;

        public string UserName { get; set; } = null!;

        public string Password { get; set; } = null!;

        public string Salt { get; set; } = null!;

        public string? Contact { get; set; }


        public string Email { get; set; } = null!;


        public string? Phone { get; set; }

        public string? Position { get; set; }

        public string? Image { get; set; }

        public bool IsActive { get; set; }

        public int UserType { get; set; }

        public DateTime Created { get; set; }
    }
}
