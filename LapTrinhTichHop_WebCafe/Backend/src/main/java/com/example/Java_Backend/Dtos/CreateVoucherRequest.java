package com.example.Java_Backend.Dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateVoucherRequest {

    private String code;

    private String title;

    private String discountType;

    private BigDecimal discountValue;

    private BigDecimal maxDiscount;

    private BigDecimal minOrderValue;

    private Integer usageLimit;

    private String paymentMethod;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive;
}