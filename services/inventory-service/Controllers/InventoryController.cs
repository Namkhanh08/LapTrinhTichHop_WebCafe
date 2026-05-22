using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using InventoryService.Data;
using InventoryService.Models;

namespace InventoryService.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class InventoryController : ControllerBase
    {
        private readonly InventoryDbContext _context;

        public InventoryController(InventoryDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<ActionResult<IEnumerable<InventoryItem>>> GetInventory()
        {
            var items = await _context.InventoryItems.ToListAsync();
            return Ok(new { items, total = items.Count });
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<InventoryItem>> GetItem(int id)
        {
            var item = await _context.InventoryItems.FindAsync(id);
            if (item == null) return NotFound(new { error = "Inventory item not found" });
            return Ok(item);
        }

        [HttpGet("product/{productId}")]
        public async Task<ActionResult<InventoryItem>> GetByProduct(string productId)
        {
            var item = await _context.InventoryItems.FirstOrDefaultAsync(i => i.ProductId == productId);
            if (item == null) return NotFound(new { error = "No inventory record for this product" });
            return Ok(item);
        }

        [HttpPost]
        public async Task<ActionResult<InventoryItem>> CreateItem([FromBody] InventoryItem item)
        {
            item.CreatedAt = DateTime.UtcNow;
            item.UpdatedAt = DateTime.UtcNow;
            _context.InventoryItems.Add(item);
            await _context.SaveChangesAsync();

            // Log initial stock movement if quantity > 0
            if (item.QuantityAvailable > 0)
            {
                _context.StockMovements.Add(new StockMovement
                {
                    ProductId = item.ProductId,
                    Quantity = item.QuantityAvailable,
                    MovementType = MovementType.In,
                    ReferenceType = "initial_stock",
                    Notes = "Initial inventory creation"
                });
                await _context.SaveChangesAsync();
            }

            return CreatedAtAction(nameof(GetItem), new { id = item.Id }, item);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateItem(int id, [FromBody] InventoryItem item)
        {
            if (id != item.Id) return BadRequest(new { error = "ID mismatch" });

            var existing = await _context.InventoryItems.FindAsync(id);
            if (existing == null) return NotFound();

            var oldQty = existing.QuantityAvailable;

            existing.ProductName = item.ProductName ?? existing.ProductName;
            existing.QuantityAvailable = item.QuantityAvailable;
            existing.QuantityReserved = item.QuantityReserved;
            existing.WarehouseLocation = item.WarehouseLocation ?? existing.WarehouseLocation;
            existing.ReorderLevel = item.ReorderLevel;
            existing.UpdatedAt = DateTime.UtcNow;

            // Log adjustment if quantity changed
            if (oldQty != item.QuantityAvailable)
            {
                _context.StockMovements.Add(new StockMovement
                {
                    ProductId = existing.ProductId,
                    Quantity = item.QuantityAvailable - oldQty,
                    MovementType = MovementType.Adjustment,
                    ReferenceType = "manual_adjustment",
                    Notes = $"Manual adjustment from {oldQty} to {item.QuantityAvailable}"
                });
            }

            await _context.SaveChangesAsync();
            return Ok(existing);
        }

        [HttpPost("reserve")]
        public async Task<IActionResult> ReserveStock([FromBody] ReserveRequest request)
        {
            var item = await _context.InventoryItems.FirstOrDefaultAsync(i => i.ProductId == request.ProductId);
            if (item == null) return NotFound(new { error = "Product not found in inventory" });

            var available = item.QuantityAvailable - item.QuantityReserved;
            if (available < request.Quantity)
                return BadRequest(new { error = "Insufficient stock", available, requested = request.Quantity });

            item.QuantityReserved += request.Quantity;
            item.UpdatedAt = DateTime.UtcNow;

            _context.StockMovements.Add(new StockMovement
            {
                ProductId = request.ProductId,
                Quantity = request.Quantity,
                MovementType = MovementType.Reserve,
                ReferenceId = request.OrderId,
                ReferenceType = "order",
                Notes = $"Reserved for order {request.OrderId}"
            });

            await _context.SaveChangesAsync();
            return Ok(new { message = "Stock reserved", productId = request.ProductId, reserved = request.Quantity, available = item.QuantityAvailable - item.QuantityReserved });
        }

        [HttpPost("release")]
        public async Task<IActionResult> ReleaseStock([FromBody] ReserveRequest request)
        {
            var item = await _context.InventoryItems.FirstOrDefaultAsync(i => i.ProductId == request.ProductId);
            if (item == null) return NotFound();

            item.QuantityReserved = Math.Max(0, item.QuantityReserved - request.Quantity);
            item.UpdatedAt = DateTime.UtcNow;

            _context.StockMovements.Add(new StockMovement
            {
                ProductId = request.ProductId,
                Quantity = request.Quantity,
                MovementType = MovementType.Release,
                ReferenceId = request.OrderId,
                ReferenceType = "order",
                Notes = $"Released reservation for order {request.OrderId}"
            });

            await _context.SaveChangesAsync();
            return Ok(new { message = "Reservation released", productId = request.ProductId, released = request.Quantity });
        }

        [HttpPost("commit")]
        public async Task<IActionResult> CommitStock([FromBody] ReserveRequest request)
        {
            var item = await _context.InventoryItems.FirstOrDefaultAsync(i => i.ProductId == request.ProductId);
            if (item == null) return NotFound();

            var reservedToCommit = Math.Min(item.QuantityReserved, request.Quantity);
            item.QuantityReserved -= reservedToCommit;
            item.QuantityAvailable = Math.Max(0, item.QuantityAvailable - request.Quantity);
            item.UpdatedAt = DateTime.UtcNow;

            _context.StockMovements.Add(new StockMovement
            {
                ProductId = request.ProductId,
                Quantity = -request.Quantity,
                MovementType = MovementType.Out,
                ReferenceId = request.OrderId,
                ReferenceType = "order",
                Notes = $"Committed stock for delivered order {request.OrderId}"
            });

            await _context.SaveChangesAsync();
            return Ok(new { message = "Stock committed", productId = request.ProductId, committed = request.Quantity });
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteItem(int id)
        {
            var item = await _context.InventoryItems.FindAsync(id);
            if (item == null) return NotFound();

            _context.InventoryItems.Remove(item);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }

    public class ReserveRequest
    {
        public string ProductId { get; set; } = string.Empty;
        public int Quantity { get; set; }
        public string? OrderId { get; set; }
    }
}

