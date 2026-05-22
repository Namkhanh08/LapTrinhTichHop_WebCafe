package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import com.example.Java_Backend.entities.Product;
import com.example.Java_Backend.entities.Order;
import lombok.Data;

@Data
public class OrderDetail {
    private Integer Id;

    private Integer OrderId;

    private Integer ProductId;

    private Integer Quantity;

    private Double UnitPrice;

    private String FlavorNotes;

    private Integer GrindingOptionId;

    private String Weight;

    private Product Product;

    private Order Order;
}
