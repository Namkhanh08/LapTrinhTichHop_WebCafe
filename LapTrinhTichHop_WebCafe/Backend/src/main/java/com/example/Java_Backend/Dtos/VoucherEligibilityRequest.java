package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
public class VoucherEligibilityRequest {
    private List<CartItemDto> items;

    private String paymentMethod;
}
