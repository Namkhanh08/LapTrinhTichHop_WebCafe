package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class UpdateOrderDto {
    private String receiverName;

    private String receiverPhone;

    private String shippingProvince;

    private String shippingDistrict;

    private String shippingWard;

    private String shippingDetailAddress;

    private String shippingNote;

    private String paymentMethod;

    private String status;
}
