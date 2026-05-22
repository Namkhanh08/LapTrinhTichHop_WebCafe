package com.example.Java_Backend.controllers;

import com.example.Java_Backend.Dtos.DashboardDto;
import com.example.Java_Backend.services.DashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/dashboard")
@CrossOrigin("*")
public class DashboardController {
    @Autowired
    private DashboardService dasboardSer;

    @GetMapping
    public DashboardDto getDashboard(){
        return dasboardSer.getDashboardData();
    }
}
