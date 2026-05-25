package com.example.Java_Backend.controllers;

import com.example.Java_Backend.Dtos.CreateSubscriptionRequest;
import com.example.Java_Backend.Dtos.SubscriptionResponseDto;
import com.example.Java_Backend.Dtos.UpdateSubscriptionConfigRequest;
import com.example.Java_Backend.services.JwtService;
import com.example.Java_Backend.services.SubscriptionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/subscriptions")
@CrossOrigin("*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService _subscriptionService;

    @Autowired
    private JwtService jwtService;

    private String getUserIdFromHeader(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    // CREATE
    @PostMapping("/create")
    public String createSubscription(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateSubscriptionRequest dto
    ) {

        String userId = getUserIdFromHeader(authHeader);

        _subscriptionService.createSubscription(userId, dto);

        return "Đăng ký gói định kỳ thành công";
    }

    // GET USER SUBSCRIPTIONS
    @GetMapping
    public List<SubscriptionResponseDto> getUserSubscriptions(
            @RequestHeader("Authorization") String authHeader
    ) {

        String userId = getUserIdFromHeader(authHeader);

        return _subscriptionService.getUserSubscriptions(userId);
    }

    // SKIP / RESUME
    @PutMapping("/{id}/toggle-skip")
    public String toggleSkipSubscription(
            @PathVariable int id
    ) {

        _subscriptionService.toggleSkipSubscription(id);

        return "Updated subscription status";
    }

    // CANCEL
    @PutMapping("/{id}/cancel")
    public String cancelSubscription(
            @PathVariable int id
    ) {

        _subscriptionService.cancelSubscription(id);

        return "Subscription cancelled";
    }

    // UPDATE CONFIG
    @PutMapping("/{id}/config")
    public String updateConfig(
            @PathVariable int id,
            @RequestBody UpdateSubscriptionConfigRequest request
    ) {

        _subscriptionService.updateSubscriptionConfig(id, request);

        return "Updated config successfully";
    }
}