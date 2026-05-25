package com.example.Java_Backend.Dtos;

import lombok.Data;

@Data
public class UpdateSubscriptionConfigRequest {

    private String flavorNote;

    private Integer grindType;

    private String weight;
}