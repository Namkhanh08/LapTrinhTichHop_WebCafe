package com.example.Java_Backend.Dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionResponseDto {
    private int id;

    private String productName;

    private String frequency;

    private String nextBilling;

    private BigDecimal price;

    private String cardInfo;

    private String flavor;

    private String grindType;

    private String weight;

    private int quantity;

    private String status;

    private int deliveryStep;

    private String history;
}