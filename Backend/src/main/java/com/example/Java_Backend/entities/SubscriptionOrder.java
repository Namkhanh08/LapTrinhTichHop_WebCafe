package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionOrder {
    private Integer Id;

    private Integer SubscriptionId;

    private LocalDateTime DeliveryDate;

    @Lob
    private String SnapshotDetails;

    private String Status;

    private LocalDateTime CreatedAt;

    private String ReceiverName;

    private String ReceiverPhone;

    private String ShippingAddress;

    private String PaymentMethod;

    private BigDecimal FinalPrice;
}