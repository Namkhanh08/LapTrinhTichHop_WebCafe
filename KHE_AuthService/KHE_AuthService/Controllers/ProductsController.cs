using KHE_AuthService.DTOs; 
using KHE_AuthService.Entities;
using KHE_AuthService.Repositories;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace KHE_AuthService.Controllers
{
    [Route("api/auth/admin/products")]

    [ApiController]
    public class ProductsController : ControllerBase
    {
        private readonly IProductRepository _productRepo;
        public ProductsController(IProductRepository productRepo) { _productRepo = productRepo; }

        [HttpGet]
        public async Task<IActionResult> GetAll() => Ok(await _productRepo.GetAllProductsAsync());

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var product = await _productRepo.GetProductByIdAsync(id);
            if (product == null) return NotFound();
            return Ok(product);
        }

       
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] ProductRequest request)
        {
            var result = await _productRepo.AddProductAsync(request);
            if (!result) return BadRequest(new { Success = false, Message = "Thêm thất bại" });
            return Ok(new { Success = true, Message = "Thêm thành công" });
        }

   
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, [FromBody] ProductRequest request)
        {
            if (id != request.Id) return BadRequest();
            var result = await _productRepo.UpdateProductAsync(request);
            if (!result) return BadRequest(new { Success = false, Message = "Cập nhật thất bại" });
            return Ok(new { Success = true, Message = "Cập nhật thành công" });
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteProduct(int id)
        {
            var result = await _productRepo.DeleteProductAsync(id);
            if (!result) return BadRequest(new { Success = false, Message = "Xóa thất bại" });
            return Ok(new { Success = true, Message = "Xóa thành công" });
        }
    }
}
