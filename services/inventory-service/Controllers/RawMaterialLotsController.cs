using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using InventoryService.Data;
using InventoryService.Models;

namespace InventoryService.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class RawMaterialLotsController : ControllerBase
    {
        private readonly InventoryDbContext _context;

        public RawMaterialLotsController(InventoryDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<IActionResult> GetLots([FromQuery] string? beanType, [FromQuery] bool expiringSoon = false)
        {
            var query = _context.RawMaterialLots.AsQueryable();

            if (!string.IsNullOrWhiteSpace(beanType))
            {
                query = query.Where(l => l.BeanType == beanType);
            }

            if (expiringSoon)
            {
                var threshold = DateTime.UtcNow.Date.AddDays(30);
                query = query.Where(l => l.ExpirationDate <= threshold);
            }

            var items = await query.OrderBy(l => l.ExpirationDate).ToListAsync();
            return Ok(new { items, total = items.Count });
        }

        [HttpPost]
        public async Task<IActionResult> CreateLot([FromBody] RawMaterialLot lot)
        {
            var error = Validate(lot);
            if (error is not null)
            {
                return BadRequest(new { error });
            }

            lot.CreatedAt = DateTime.UtcNow;
            lot.UpdatedAt = DateTime.UtcNow;
            if (lot.QuantityRemainingKg <= 0)
            {
                lot.QuantityRemainingKg = lot.QuantityKg;
            }
            _context.RawMaterialLots.Add(lot);
            await _context.SaveChangesAsync();

            return CreatedAtAction(nameof(GetLots), new { id = lot.Id }, lot);
        }

        [HttpPut("{id:int}")]
        public async Task<IActionResult> UpdateLot(int id, [FromBody] RawMaterialLot changes)
        {
            var lot = await _context.RawMaterialLots.FindAsync(id);
            if (lot is null)
            {
                return NotFound(new { error = "Raw material lot not found" });
            }

            changes.Id = id;
            var error = Validate(changes);
            if (error is not null)
            {
                return BadRequest(new { error });
            }

            lot.BeanType = changes.BeanType;
            lot.RawMaterialTypeId = changes.RawMaterialTypeId;
            lot.Supplier = changes.Supplier;
            lot.SupplierId = changes.SupplierId;
            lot.QuantityKg = changes.QuantityKg;
            lot.QuantityRemainingKg = changes.QuantityRemainingKg > 0 ? changes.QuantityRemainingKg : changes.QuantityKg;
            lot.ReceivedDate = changes.ReceivedDate;
            lot.ExpirationDate = changes.ExpirationDate;
            lot.OriginRegion = changes.OriginRegion;
            lot.Notes = changes.Notes;
            lot.UpdatedAt = DateTime.UtcNow;

            await _context.SaveChangesAsync();
            return Ok(lot);
        }

        [HttpDelete("{id:int}")]
        public async Task<IActionResult> DeleteLot(int id)
        {
            var lot = await _context.RawMaterialLots.FindAsync(id);
            if (lot is null)
            {
                return NotFound(new { error = "Raw material lot not found" });
            }

            _context.RawMaterialLots.Remove(lot);
            await _context.SaveChangesAsync();
            return NoContent();
        }

        private static string? Validate(RawMaterialLot lot)
        {
            if (string.IsNullOrWhiteSpace(lot.BeanType))
            {
                return "beanType is required";
            }
            if (string.IsNullOrWhiteSpace(lot.Supplier))
            {
                return "supplier is required";
            }
            if (lot.QuantityKg <= 0)
            {
                return "quantityKg must be greater than zero";
            }
            if (lot.QuantityRemainingKg < 0)
            {
                return "quantityRemainingKg cannot be negative";
            }
            if (lot.QuantityRemainingKg > lot.QuantityKg)
            {
                return "quantityRemainingKg cannot be greater than quantityKg";
            }
            if (lot.ExpirationDate.Date < lot.ReceivedDate.Date)
            {
                return "expirationDate must be greater than or equal to receivedDate";
            }
            if (lot.ReceivedDate.Date > DateTime.UtcNow.Date)
            {
                return "receivedDate cannot be in the future";
            }
            return null;
        }
    }
}
