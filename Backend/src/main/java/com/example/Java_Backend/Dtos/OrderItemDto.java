package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class OrderItemDto {
    private Integer ProductId;

    private Integer Quantity;

    private Integer GrindingOptionId;

    private String FlavorNotes;

    private String Weight;
}
