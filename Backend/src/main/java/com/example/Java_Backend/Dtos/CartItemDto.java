package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
public class CartItemDto {
    private long productId;

    private Integer quantity;

}
