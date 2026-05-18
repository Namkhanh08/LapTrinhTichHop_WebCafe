package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class Product {
    private Integer Id;

    private String Name;

    private Double Price;

    private Integer Stock;

    private String ImageUrl;

    private String Description;

    private Integer CategoryId;
}