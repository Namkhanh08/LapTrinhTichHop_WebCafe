package com.example.order_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    @Column(name = "user_id", nullable = false)
    @JsonAlias("UserId")
    private Long userId;

    @Column(name = "subscription_id")
    @JsonAlias("SubscriptionId")
    private Long subscriptionId;

    @Column(name = "user_email")
    @JsonAlias("UserEmail")
    private String userEmail;

    @Column(name = "user_name")
    @JsonAlias("UserName")
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.pending;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // --- Bổ sung các cột thông tin người nhận từ BC2.sql ---
    @Column(name = "receiver_name")
    @JsonAlias("ReceiverName")
    private String receiverName;

    @Column(name = "receiver_phone")
    @JsonAlias("ReceiverPhone")
    private String receiverPhone;

    @Column(name = "receiver_email")
    @JsonAlias("ReceiverEmail")
    private String receiverEmail;

    // --- Bổ sung các cột địa chỉ giao hàng phân cấp Việt Nam từ BC2.sql ---
    @Column(name = "shipping_province")
    @JsonAlias("ShippingProvince")
    private String shippingProvince;

    @Column(name = "shipping_district")
    @JsonAlias("ShippingDistrict")
    private String shippingDistrict;

    @Column(name = "shipping_ward")
    @JsonAlias("ShippingWard")
    private String shippingWard;

    @Column(name = "shipping_detail_address")
    @JsonAlias("ShippingDetailAddress")
    private String shippingDetailAddress;

    @Column(name = "shipping_note")
    @JsonAlias("ShippingNote")
    private String shippingNote;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    @JsonAlias("ShippingAddress")
    private String shippingAddress;

    @Column(name = "shipping_phone")
    @JsonAlias("ShippingPhone")
    private String shippingPhone;

    @Column(name = "payment_method")
    @JsonAlias("PaymentMethod")
    private String paymentMethod = "cod";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.pending;

    // --- Bổ sung các cột Voucher giảm giá từ BC2.sql ---
    @Column(name = "voucher_code")
    @JsonAlias("VoucherCode")
    private String voucherCode;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @JsonAlias("DiscountAmount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 12, scale = 2)
    @JsonAlias("FinalAmount")
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonAlias({"Items", "OrderDetails"})
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        // Tự động tính toán FinalAmount sau khi trừ discount
        if (this.discountAmount != null) {
            this.finalAmount = this.totalAmount.subtract(this.discountAmount);
            if (this.finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                this.finalAmount = BigDecimal.ZERO;
            }
        } else {
            this.finalAmount = this.totalAmount;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }

    public String getReceiverEmail() { return receiverEmail; }
    public void setReceiverEmail(String receiverEmail) { this.receiverEmail = receiverEmail; }

    public String getShippingProvince() { return shippingProvince; }
    public void setShippingProvince(String shippingProvince) { this.shippingProvince = shippingProvince; }

    public String getShippingDistrict() { return shippingDistrict; }
    public void setShippingDistrict(String shippingDistrict) { this.shippingDistrict = shippingDistrict; }

    public String getShippingWard() { return shippingWard; }
    public void setShippingWard(String shippingWard) { this.shippingWard = shippingWard; }

    public String getShippingDetailAddress() { return shippingDetailAddress; }
    public void setShippingDetailAddress(String shippingDetailAddress) { this.shippingDetailAddress = shippingDetailAddress; }

    public String getShippingNote() { return shippingNote; }
    public void setShippingNote(String shippingNote) { this.shippingNote = shippingNote; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- PascalCase Support ---
    @JsonProperty("Id")
    public Long getPascalId() { return id; }

    @JsonProperty("OrderDate")
    public LocalDateTime getPascalOrderDate() { return createdAt; }

    @JsonProperty("Status")
    public String getPascalStatus() {
        if (status == OrderStatus.pending) return "Chờ thanh toán";
        if (status == OrderStatus.confirmed) return "Đã xác nhận";
        if (status == OrderStatus.processing) return "Chờ xử lý";
        if (status == OrderStatus.shipped) return "Đang giao";
        if (status == OrderStatus.delivered) return "Hoàn thành";
        if (status == OrderStatus.cancelled) return "Đã hủy";
        return status != null ? status.name() : "Chờ thanh toán";
    }

    @JsonProperty("FinalAmount")
    public BigDecimal getPascalFinalAmount() { return finalAmount; }

    @JsonProperty("TotalAmount")
    public BigDecimal getPascalTotalAmount() { return totalAmount; }

    @JsonProperty("OrderDetails")
    public List<OrderItem> getPascalOrderDetails() { return items; }

    @JsonProperty("ReceiverName")
    public String getPascalReceiverName() { return receiverName; }

    @JsonProperty("ReceiverPhone")
    public String getPascalReceiverPhone() { return receiverPhone; }

    @JsonProperty("ShippingProvince")
    public String getPascalShippingProvince() { return shippingProvince; }

    @JsonProperty("ShippingDistrict")
    public String getPascalShippingDistrict() { return shippingDistrict; }

    @JsonProperty("ShippingWard")
    public String getPascalShippingWard() { return shippingWard; }

    @JsonProperty("ShippingDetailAddress")
    public String getPascalShippingDetailAddress() { return shippingDetailAddress; }

    @JsonProperty("PaymentMethod")
    public String getPascalPaymentMethod() { return paymentMethod; }

    @JsonProperty("VoucherCode")
    public String getPascalVoucherCode() { return voucherCode; }

    @JsonProperty("DiscountAmount")
    public BigDecimal getPascalDiscountAmount() { return discountAmount; }

    public enum OrderStatus {
        pending, confirmed, processing, shipped, delivered, cancelled
    }

    public enum PaymentStatus {
        pending, paid, failed, refunded
    }
}
