package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import java.util.List;
import lombok.Data;

@Data
public class TopProductDto {
    private int productId;

    private String productName;

    private String imageUrl;

    private int totalSold;
}
