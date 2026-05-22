package com.example.order_service.controller;

import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.repository.OrderRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final OrderRepository orderRepository;

    public DashboardController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public Map<String, Object> getDashboard() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Expected Revenue: sum of finalAmount for orders that are not cancelled
        BigDecimal expectedRevenue = allOrders.stream()
            .filter(o -> o.getStatus() != Order.OrderStatus.cancelled)
            .map(Order::getFinalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Pending orders: pending, processing, confirmed
        long pendingOrders = allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.pending 
                      || o.getStatus() == Order.OrderStatus.processing
                      || o.getStatus() == Order.OrderStatus.confirmed)
            .count();

        // 3. Latest orders: top 5 sorted by createdAt desc
        List<Order> latestOrders = allOrders.stream()
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .limit(5)
            .toList();

        // 4. Top products: group by productId and sum quantity
        Map<String, Map<String, Object>> productSales = new HashMap<>();
        for (Order order : allOrders) {
            if (order.getStatus() == Order.OrderStatus.cancelled) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                String pid = item.getProductId();
                if (pid == null) continue;
                
                productSales.compute(pid, (k, v) -> {
                    if (v == null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("productId", pid);
                        map.put("productName", item.getProductName() != null ? item.getProductName() : ("Sản phẩm " + pid));
                        map.put("totalSold", (long) item.getQuantity());
                        return map;
                    } else {
                        long currentSold = (long) v.get("totalSold");
                        v.put("totalSold", currentSold + item.getQuantity());
                        return v;
                    }
                });
            }
        }

        List<Map<String, Object>> topProducts = productSales.values().stream()
            .sorted((p1, p2) -> Long.compare((long) p2.get("totalSold"), (long) p1.get("totalSold")))
            .limit(5)
            .toList();

        return Map.of(
            "expectedRevenue", expectedRevenue,
            "pendingOrders", pendingOrders,
            "latestOrders", latestOrders,
            "topProducts", topProducts
        );
    }
}
