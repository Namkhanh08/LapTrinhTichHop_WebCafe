package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
public class SubscriptionConfig {
    private Integer Id;

    private Integer SubscriptionId;

    private Integer ProductId;

    private String FlavorNote;

    private Integer GrindType;

    private String Weight;

    private Integer Quantity;
}