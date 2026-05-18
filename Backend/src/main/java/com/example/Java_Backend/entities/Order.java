package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import com.example.Java_Backend.entities.OrderDetail;


import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Integer Id;

    private String UserId;

    private LocalDateTime OrderDate;

    private Double TotalAmount;

    private String Status;

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


    private List<OrderDetail> OrderDetails;
}
