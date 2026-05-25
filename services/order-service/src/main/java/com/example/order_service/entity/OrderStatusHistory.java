package com.example.order_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_code")
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private Order.OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private Order.OrderStatus toStatus;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public Order.OrderStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(Order.OrderStatus fromStatus) { this.fromStatus = fromStatus; }

    public Order.OrderStatus getToStatus() { return toStatus; }
    public void setToStatus(Order.OrderStatus toStatus) { this.toStatus = toStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
