package com.example.Java_Backend.Dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateSubscriptionRequest {
    // Thông tin định cấu hình hạt (SubscriptionConfigs)
    private Integer productId;
    private String flavorNotes;
    private Integer grindType;
    private String weight;
    private Integer quantity;

    // Thông tin chu kỳ & cam kết (Subscriptions)
    private String frequency;       // '1week', '2weeks', '1month'
    private String deliveryDay;     // 'Tuesday', 'Thursday'
    private String commitment;      // 'pay-as-you-go', '3months', '6months'
    private String paymentMethod;   // 'card_auto', 'vnpay'

    // Thông tin người nhận (Dùng cho SubscriptionOrders đầu tiên hoặc lưu trữ)
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private BigDecimal totalPrice;  // Giá sau khi giảm trừ theo chu kỳ/cam kết
}