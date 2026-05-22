package com.example.Java_Backend.services;

import com.example.Java_Backend.Dtos.DashboardDto;
import com.example.Java_Backend.repositories.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository oRepo;

    public DashboardDto getDashboardData(){

        DashboardDto dto = new DashboardDto();

        dto.setExpectedRevenue(
                oRepo.getExpectedRevenue()
        );

        dto.setPendingOrders(
                oRepo.getPendingOrdersCount()
        );

        dto.setLatestOrders(
                oRepo.getLastestOrders()
        );

        dto.setTopProducts(
                oRepo.getTopProducts()
        );

        return dto;
    }
}