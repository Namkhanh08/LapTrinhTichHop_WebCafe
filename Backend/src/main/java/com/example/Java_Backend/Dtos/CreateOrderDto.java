package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import com.example.Java_Backend.Dtos.OrderItemDto;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDto {
    private List<OrderItemDto> Items;

    private String ReceiverName;

    private String ReceiverPhone;

    private String ShippingProvince;

    private String ShippingDistrict;

    private String ShippingWard;

    private String ShippingDetailAddress;

    private String ShippingNote;

    private String PaymentMethod;

    private String VoucherCode;

    private Double DiscountAmount;

    private Double FinalAmount;

}
