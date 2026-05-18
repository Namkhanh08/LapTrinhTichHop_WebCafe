package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
public class CartItem {
    private Integer Id;

    private Integer CartId;

    private Integer ProductId;

    private Integer Quantity;

    private String FlavorNotes;

    private Integer GrindingOptionId;

    private String Weight;

    private Product Product;
}
