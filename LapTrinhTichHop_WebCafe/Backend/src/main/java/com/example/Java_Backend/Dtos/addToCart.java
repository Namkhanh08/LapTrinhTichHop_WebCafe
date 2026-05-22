package com.example.Java_Backend.Dtos;

import lombok.Data;

@Data
public class addToCart {

    private Integer productId;

    private Integer quantity;

    private String flavorNotes;

    private Integer grindingOptionId;

    private String weight;

}
