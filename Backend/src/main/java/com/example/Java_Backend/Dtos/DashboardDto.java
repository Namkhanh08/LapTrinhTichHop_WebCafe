package com.example.Java_Backend.Dtos;

import jakarta.persistence.*;
import com.example.Java_Backend.entities.Order;
import com.example.Java_Backend.Dtos.TopProductDto;

import java.util.List;
import lombok.Data;

@Data
public class DashboardDto {
    private double expectedRevenue;

    private int pendingOrders;

    private List<Order> latestOrders;

    private List<TopProductDto> topProducts;
}
