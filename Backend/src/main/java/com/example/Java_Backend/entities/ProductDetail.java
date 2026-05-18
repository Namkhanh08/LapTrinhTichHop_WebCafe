package com.example.Java_Backend.entities;

import com.example.Java_Backend.entities.Product;
import com.example.Java_Backend.entities.ProductGrindingOption;
import com.example.Java_Backend.entities.GrindingOption;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetail {
    private Integer Id;

    private Integer ProductId;

    private String Region;

    private String Process;

    private String Roast;

    private String FlavorNotes;

    private String Weight;

    private String Height;

    private Product product;

    private List<GrindingOption> GrindingOption;

}
