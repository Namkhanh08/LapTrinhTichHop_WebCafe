package com.example.Java_Backend.controllers;

import com.example.Java_Backend.entities.CartItem;
import com.example.Java_Backend.entities.Cart;
import com.example.Java_Backend.Dtos.addToCart;
import com.example.Java_Backend.services.CartService;
import com.example.Java_Backend.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/carts")
@CrossOrigin("*")
public class CartController {

    @Autowired
    private CartService _cartSer;
    @Autowired
    private JwtService jwtService;

    private String getUserIdFromHeader(String authHeader){
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    @GetMapping
    public Object getCart(@RequestHeader("Authorization") String authHeader){
        String userId = getUserIdFromHeader(authHeader);
        return _cartSer.getCart(userId);
    }

    @PostMapping("/add")
    public String addToCart(@RequestHeader("Authorization") String authHeader, @RequestBody addToCart dto){
        String userId = getUserIdFromHeader(authHeader);
        _cartSer.addToCart(userId, dto.getProductId(), dto.getQuantity(), dto.getGrindingOptionId(), dto.getFlavorNotes(), dto.getWeight());
        return "Đã thêm vào giỏ hàng";
    }

    @PutMapping("/update")
    public String updateCart(@RequestHeader("Authorization") String authHeader, @RequestBody addToCart dto){
        String userId = getUserIdFromHeader(authHeader);
        _cartSer.uppdateCartItem(userId, dto.getProductId(), dto.getQuantity(), dto.getGrindingOptionId(), dto.getFlavorNotes(), dto.getWeight());
        return "Đã cập nhật giỏ hàng";
    }

    @DeleteMapping("/remove")
    public String removeItem(@RequestBody addToCart dto){
        _cartSer.removeItem(dto.getProductId(), dto.getGrindingOptionId(), dto.getFlavorNotes(), dto.getWeight());
        return "Đã xóa sản phẩm";
    }
}
