package com.example.order_service.controller;

import com.example.order_service.service.CartService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final ObjectMapper objectMapper;

    public CartController(CartService cartService, ObjectMapper objectMapper) {
        this.cartService = cartService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Map<String, Object> getCart(@RequestHeader("Authorization") String authorization) {
        return cartService.getCart(readUserId(authorization));
    }

    @PostMapping("/add")
    public Map<String, String> addToCart(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CartRequest request) {
        cartService.addToCart(readUserId(authorization), request);
        return Map.of("message", "Added to cart");
    }

    @PutMapping("/update")
    public Map<String, String> updateCart(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CartRequest request) {
        cartService.updateCartItem(readUserId(authorization), request);
        return Map.of("message", "Cart updated");
    }

    @DeleteMapping("/remove")
    public Map<String, String> removeItem(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CartRequest request) {
        cartService.removeItem(readUserId(authorization), request);
        return Map.of("message", "Cart item removed");
    }

    private Long readUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        String[] parts = authorization.substring("Bearer ".length()).split("\\.");
        if (parts.length != 3) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token");
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode json = objectMapper.readTree(new String(payload, StandardCharsets.UTF_8));
            return json.path("sub").asLong();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid bearer token");
        }
    }

    public static class CartRequest {
        private String productId;
        private Integer quantity;
        private Integer grindingOptionId;
        private String flavorNotes;
        private String weight;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Integer getGrindingOptionId() { return grindingOptionId; }
        public void setGrindingOptionId(Integer grindingOptionId) { this.grindingOptionId = grindingOptionId; }
        public String getFlavorNotes() { return flavorNotes; }
        public void setFlavorNotes(String flavorNotes) { this.flavorNotes = flavorNotes; }
        public String getWeight() { return weight; }
        public void setWeight(String weight) { this.weight = weight; }
    }
}
