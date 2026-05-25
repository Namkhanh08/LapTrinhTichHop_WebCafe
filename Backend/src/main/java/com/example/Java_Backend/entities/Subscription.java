package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Subscription {
    private Integer Id;

    private String UserId;

    private String Frequency;

    private String DeliveryDay;

    private String Status;

    private LocalDateTime NextDeliveryDate;

    private LocalDateTime CreatedAt;

    private Integer CommitmentMonths;

    private LocalDateTime CommitmentEndDate;
}