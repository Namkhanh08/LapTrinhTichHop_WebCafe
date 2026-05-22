package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import com.example.Java_Backend.entities.GrindingOption;
import lombok.Data;

@Data
public class ProductGrindingOption {
    private Integer ProductId;

    private Integer GrindingOptionId;

    private GrindingOption GrindingOption;

}
