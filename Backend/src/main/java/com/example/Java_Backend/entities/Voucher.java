package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Voucher {
    private Long Id;

    private String Code;

    private String Title;

    private String DiscountType;

    private BigDecimal DiscountValue;

    private BigDecimal MaxDiscount;

    private BigDecimal MinOrderValue;

    private Integer UsedCount;

    private Integer UsageLimit;

    private String PaymentMethod;

    private LocalDate StartDate;

    private LocalDate EndDate;

    private Boolean IsActive;
}
