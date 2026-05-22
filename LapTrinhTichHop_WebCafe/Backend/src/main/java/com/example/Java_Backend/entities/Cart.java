package com.example.Java_Backend.entities;

import jakarta.persistence.*;
import com.example.Java_Backend.entities.CartItem;
import lombok.Data;
import java.util.List;

@Data
public class Cart {
    private Integer Id;

    private String UserId;

    private List<CartItem> items;

}
