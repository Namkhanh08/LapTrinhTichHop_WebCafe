package com.example.order_service.service;

import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final VoucherService voucherService; // Tiêm VoucherService từ BC2.sql migration

    public OrderService(OrderRepository orderRepository, InventoryClient inventoryClient, VoucherService voucherService) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.voucherService = voucherService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public Order getOrderByCode(String code) {
        return orderRepository.findByOrderCode(code)
            .orElseThrow(() -> new RuntimeException("Order not found with code: " + code));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order createOrder(Order order) {
        validateNewOrder(order);

        order.setOrderCode(generateOrderCode());
        order.setStatus(Order.OrderStatus.pending);
        order.setPaymentStatus(Order.PaymentStatus.pending);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Gán order cho từng order item và tính toán tiền hàng
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
                item.calculateSubtotal();
            }
            order.calculateTotal();
        }

        // Tích hợp áp dụng giảm giá voucher thực tế từ BC2.sql migration
        if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
            BigDecimal discount = voucherService.calculateDiscount(order.getVoucherCode(), order.getTotalAmount());
            order.setDiscountAmount(discount);
            BigDecimal finalAmount = order.getTotalAmount().subtract(discount);
            order.setFinalAmount(finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount);
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setFinalAmount(order.getTotalAmount());
        }

        List<OrderItem> reservedItems = new java.util.ArrayList<>();
        try {
            for (OrderItem item : order.getItems()) {
                inventoryClient.reserve(item, order.getOrderCode());
                reservedItems.add(item);
            }
            
            Order savedOrder = orderRepository.save(order);
            
            // Tăng số lượt dùng voucher sau khi đặt đơn thành công
            if (order.getVoucherCode() != null && !order.getVoucherCode().isBlank()) {
                voucherService.incrementUsedCount(order.getVoucherCode());
            }
            
            return savedOrder;
        } catch (RuntimeException ex) {
            for (OrderItem item : reservedItems) {
                try {
                    inventoryClient.release(item, order.getOrderCode());
                } catch (RuntimeException ignored) {
                    // Best-effort compensation for reservations already made before the failure.
                }
            }
            throw ex;
        }
    }

    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        if (status == Order.OrderStatus.cancelled) {
            return cancelOrder(id, null);
        }
        if (status == Order.OrderStatus.delivered) {
            return confirmDelivered(id);
        }

        Order order = getOrderById(id);
        if (order.getStatus() == Order.OrderStatus.cancelled || order.getStatus() == Order.OrderStatus.delivered) {
            throw new IllegalStateException("Cannot update a completed or cancelled order");
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long id, String cancelReason) {
        Order order = getOrderById(id);
        if (order.getStatus() == Order.OrderStatus.delivered) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == Order.OrderStatus.cancelled) {
            return order;
        }

        for (OrderItem item : order.getItems()) {
            inventoryClient.release(item, order.getOrderCode());
        }

        order.setStatus(Order.OrderStatus.cancelled);
        order.setCancelReason(normalizeCancelReason(cancelReason));
        order.setPaymentStatus(Order.PaymentStatus.refunded);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order confirmDelivered(Long id) {
        Order order = getOrderById(id);
        if (order.getStatus() == Order.OrderStatus.cancelled) {
            throw new IllegalStateException("Cannot complete a cancelled order");
        }
        if (order.getStatus() == Order.OrderStatus.delivered) {
            return order;
        }

        for (OrderItem item : order.getItems()) {
            inventoryClient.commit(item, order.getOrderCode());
        }

        order.setStatus(Order.OrderStatus.delivered);
        order.setPaymentStatus(Order.PaymentStatus.paid);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order updatePaymentStatus(Long id, Order.PaymentStatus paymentStatus) {
        Order order = getOrderById(id);
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order changes) {
        Order order = getOrderById(id);
        if (order.getStatus() == Order.OrderStatus.cancelled || order.getStatus() == Order.OrderStatus.delivered) {
            throw new IllegalStateException("Cannot update a completed or cancelled order");
        }

        order.setReceiverName(changes.getReceiverName());
        order.setReceiverPhone(changes.getReceiverPhone());
        order.setReceiverEmail(changes.getReceiverEmail());
        order.setShippingProvince(changes.getShippingProvince());
        order.setShippingDistrict(changes.getShippingDistrict());
        order.setShippingWard(changes.getShippingWard());
        order.setShippingDetailAddress(changes.getShippingDetailAddress());
        order.setShippingAddress(changes.getShippingAddress());
        order.setShippingPhone(changes.getShippingPhone());
        order.setShippingNote(changes.getShippingNote());
        order.setPaymentMethod(changes.getPaymentMethod());
        order.setNotes(changes.getNotes());
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    private void validateNewOrder(Order order) {
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null || item.getProductId().isBlank()) {
                throw new IllegalArgumentException("items.productId is required");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("items.quantity must be greater than zero");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("items.unitPrice must be zero or greater");
            }
        }
    }

    private String generateOrderCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String random = String.valueOf((int)(Math.random() * 1000));
        return "ORD-" + timestamp + "-" + random;
    }

    private String normalizeCancelReason(String cancelReason) {
        if (cancelReason == null || cancelReason.isBlank()) {
            return "Khach hang khong cung cap ly do";
        }
        return cancelReason.trim();
    }

    public Map<String, Object> fetchAllOrdersAdmin(int page, String searchTerm, String status) {
        List<Order> allOrders = orderRepository.findAll();
        
        List<Order> filtered = allOrders.stream()
            .filter(o -> {
                if ("all".equalsIgnoreCase(status)) return true;
                String vietStatus = o.getPascalStatus();
                String engStatus = o.getStatus().name();
                return status.equalsIgnoreCase(vietStatus) || status.equalsIgnoreCase(engStatus);
            })
            .filter(o -> {
                if (searchTerm == null || searchTerm.isBlank()) return true;
                String term = searchTerm.toLowerCase();
                boolean matchId = String.valueOf(o.getId()).contains(term);
                boolean matchReceiver = o.getReceiverName() != null && o.getReceiverName().toLowerCase().contains(term);
                boolean matchPhone = o.getReceiverPhone() != null && o.getReceiverPhone().toLowerCase().contains(term);
                boolean matchAddress = o.getShippingDetailAddress() != null && o.getShippingDetailAddress().toLowerCase().contains(term);
                return matchId || matchReceiver || matchPhone || matchAddress;
            })
            .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
            .toList();
            
        int pageSize = 10;
        int totalItems = filtered.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        
        List<Order> paginated = (fromIndex < totalItems && fromIndex >= 0)
            ? filtered.subList(fromIndex, toIndex)
            : java.util.Collections.emptyList();
            
        return Map.of(
            "items", paginated,
            "totalItems", totalItems,
            "page", page,
            "pageSize", pageSize
        );
    }
}
