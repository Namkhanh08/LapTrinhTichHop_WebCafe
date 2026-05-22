using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using InventoryService.Data;
using InventoryService.Models;

namespace InventoryService.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class StockMovementsController : ControllerBase
    {
        private readonly InventoryDbContext _context;

        public StockMovementsController(InventoryDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<ActionResult<IEnumerable<StockMovement>>> GetMovements(
            [FromQuery] string? productId,
            [FromQuery] string? type,
            [FromQuery] int page = 1,
            [FromQuery] int pageSize = 20)
        {
            var query = _context.StockMovements.AsQueryable();

            if (!string.IsNullOrEmpty(productId))
                query = query.Where(m => m.ProductId == productId);

            if (!string.IsNullOrEmpty(type) && Enum.TryParse<MovementType>(type, true, out var movementType))
                query = query.Where(m => m.MovementType == movementType);

            var total = await query.CountAsync();
            var items = await query
                .OrderByDescending(m => m.CreatedAt)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return Ok(new { items, total, page, pageSize });
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<StockMovement>> GetMovement(int id)
        {
            var movement = await _context.StockMovements.FindAsync(id);
            if (movement == null) return NotFound();
            return Ok(movement);
        }

        [HttpPost]
        public async Task<ActionResult<StockMovement>> CreateMovement([FromBody] StockMovement movement)
        {
            movement.CreatedAt = DateTime.UtcNow;
            _context.StockMovements.Add(movement);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetMovement), new { id = movement.Id }, movement);
        }
    }
}

