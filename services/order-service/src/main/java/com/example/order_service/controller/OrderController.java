package com.example.order_service.controller;

import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderStatusHistory;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public Map<String, Object> getAllOrders(@RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = tryReadUserId(authorization);
        List<Order> orders = userId != null
            ? orderService.getOrdersByUser(userId)
            : orderService.getAllOrders();
        return Map.of("items", orders, "total", orders.size());
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        return Map.of("items", orders, "total", orders.size());
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/{id}/status-history")
    public Map<String, Object> getStatusHistory(@PathVariable Long id) {
        List<OrderStatusHistory> items = orderService.getStatusHistory(id);
        return Map.of("items", items, "total", items.size());
    }

    @PostMapping
    public Order createOrder(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Order order) {
        Long userId = tryReadUserId(authorization);
        if (userId != null) {
            order.setUserId(userId);
        }
        return orderService.createOrder(order);
    }

    @GetMapping("/admin/all")
    public Map<String, Object> fetchAllOrdersAdmin(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "searchTerm", defaultValue = "") String searchTerm,
            @RequestParam(value = "status", defaultValue = "all") String status) {
        return orderService.fetchAllOrdersAdmin(page, searchTerm, status);
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam(value = "status", required = false) String paramStatus,
            @RequestBody(required = false) Map<String, String> body) {
        
        String statusStr = paramStatus;
        String cancelReason = null;
        if (body != null) {
            if (statusStr == null) {
                statusStr = body.get("status");
            }
            cancelReason = body.get("cancelReason");
        }
        
        if (statusStr == null) {
            throw new IllegalArgumentException("status is required");
        }
        
        Order.OrderStatus status;
        try {
            status = Order.OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            if ("Chờ thanh toán".equals(statusStr)) status = Order.OrderStatus.pending;
            else if ("Đã xác nhận".equals(statusStr)) status = Order.OrderStatus.confirmed;
            else if ("Chờ xử lý".equals(statusStr) || "Đang xử lý".equals(statusStr)) status = Order.OrderStatus.processing;
            else if ("Đang giao".equals(statusStr)) status = Order.OrderStatus.shipped;
            else if ("Hoàn thành".equals(statusStr)) status = Order.OrderStatus.delivered;
            else if ("Đã hủy".equals(statusStr)) status = Order.OrderStatus.cancelled;
            else throw new IllegalArgumentException("Unknown status: " + statusStr);
        }
        
        if (status == Order.OrderStatus.cancelled) {
            return orderService.cancelOrder(id, cancelReason);
        }
        return orderService.updateOrderStatus(id, status);
    }

    @PostMapping("/{id}/cancel")
    public Order cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderService.cancelOrder(id, body.get("cancelReason"));
    }

    @PutMapping("/{id}/cancel")
    public Order cancelOrderFromFrontend(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        return orderService.cancelOrder(id, body != null ? body.get("cancelReason") : null);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable Long id, @RequestBody Order order) {
        return orderService.updateOrder(id, order);
    }

    @PutMapping("/{id}/confirm")
    public Order confirmOrder(@PathVariable Long id) {
        return orderService.updateOrderStatus(id, Order.OrderStatus.confirmed);
    }

    @PostMapping("/{id}/confirm-delivered")
    public Order confirmDelivered(@PathVariable Long id) {
        return orderService.confirmDelivered(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(IllegalStateException ex) {
        return Map.of("error", ex.getMessage());
    }

    private Long tryReadUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }

        String[] parts = authorization.substring("Bearer ".length()).split("\\.");
        if (parts.length != 3) {
            return null;
        }

        try {
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode json = objectMapper.readTree(new String(payload, StandardCharsets.UTF_8));
            return json.path("sub").asLong();
        } catch (Exception ex) {
            return null;
        }
    }
}
