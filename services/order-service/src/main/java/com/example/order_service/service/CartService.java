package com.example.order_service.service;

import com.example.order_service.controller.CartController.CartRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CartService {

    private final JdbcTemplate jdbcTemplate;

    public CartService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getCart(Long userId) {
        Long cartId = ensureCart(userId);
        List<Map<String, Object>> items = jdbcTemplate.query("""
            SELECT ci.id, ci.product_id, ci.quantity, ci.grinding_option_id, ci.flavor_notes, ci.weight,
                   p.name, p.price, p.image_url, p.description
            FROM cart_items ci
            LEFT JOIN revo_products.products p ON p.id = ci.product_id
            WHERE ci.cart_id = ?
            ORDER BY ci.id DESC
            """, (rs, rowNum) -> {
            Map<String, Object> product = new LinkedHashMap<>();
            product.put("Id", rs.getString("product_id"));
            product.put("Name", rs.getString("name"));
            product.put("Price", rs.getBigDecimal("price"));
            product.put("ImageUrl", rs.getString("image_url"));
            product.put("Description", rs.getString("description"));

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("Id", rs.getLong("id"));
            item.put("ProductId", rs.getString("product_id"));
            item.put("Quantity", rs.getInt("quantity"));
            item.put("GrindingOptionId", rs.getObject("grinding_option_id"));
            item.put("FlavorNotes", rs.getString("flavor_notes"));
            item.put("Weight", rs.getString("weight"));
            item.put("Product", product);
            return item;
        }, cartId);

        return Map.of("id", cartId, "userId", userId, "items", items, "total", items.size());
    }

    public void addToCart(Long userId, CartRequest request) {
        validateRequest(request, true);
        Long cartId = ensureCart(userId);
        Long existingId = findExistingItem(cartId, request);
        if (existingId != null) {
            jdbcTemplate.update(
                "UPDATE cart_items SET quantity = quantity + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                request.getQuantity(), existingId);
            return;
        }

        jdbcTemplate.update("""
            INSERT INTO cart_items (cart_id, product_id, quantity, grinding_option_id, flavor_notes, weight)
            VALUES (?, ?, ?, ?, ?, ?)
            """, cartId, request.getProductId(), request.getQuantity(), request.getGrindingOptionId(),
            request.getFlavorNotes(), request.getWeight());
    }

    public void updateCartItem(Long userId, CartRequest request) {
        validateRequest(request, false);
        Long cartId = ensureCart(userId);
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            removeItem(userId, request);
            return;
        }

        jdbcTemplate.update("""
            UPDATE cart_items
            SET quantity = ?, updated_at = CURRENT_TIMESTAMP
            WHERE cart_id = ? AND product_id = ?
              AND (grinding_option_id <=> ?)
              AND (flavor_notes <=> ?)
              AND (weight <=> ?)
            """, request.getQuantity(), cartId, request.getProductId(), request.getGrindingOptionId(),
            request.getFlavorNotes(), request.getWeight());
    }

    public void removeItem(Long userId, CartRequest request) {
        validateRequest(request, false);
        Long cartId = ensureCart(userId);
        jdbcTemplate.update("""
            DELETE FROM cart_items
            WHERE cart_id = ? AND product_id = ?
              AND (grinding_option_id <=> ?)
              AND (flavor_notes <=> ?)
              AND (weight <=> ?)
            """, cartId, request.getProductId(), request.getGrindingOptionId(),
            request.getFlavorNotes(), request.getWeight());
    }

    private Long ensureCart(Long userId) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT id FROM carts WHERE user_id = ?",
            (rs, rowNum) -> rs.getLong("id"),
            userId);
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        jdbcTemplate.update("INSERT INTO carts (user_id) VALUES (?)", userId);
        return jdbcTemplate.queryForObject(
            "SELECT id FROM carts WHERE user_id = ?",
            Long.class,
            userId);
    }

    private Long findExistingItem(Long cartId, CartRequest request) {
        List<Long> ids = jdbcTemplate.query("""
            SELECT id FROM cart_items
            WHERE cart_id = ? AND product_id = ?
              AND (grinding_option_id <=> ?)
              AND (flavor_notes <=> ?)
              AND (weight <=> ?)
            """, (rs, rowNum) -> rs.getLong("id"), cartId, request.getProductId(),
            request.getGrindingOptionId(), request.getFlavorNotes(), request.getWeight());
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void validateRequest(CartRequest request, boolean requireQuantity) {
        if (request.getProductId() == null || request.getProductId().isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
        if (requireQuantity && (request.getQuantity() == null || request.getQuantity() <= 0)) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }
}
