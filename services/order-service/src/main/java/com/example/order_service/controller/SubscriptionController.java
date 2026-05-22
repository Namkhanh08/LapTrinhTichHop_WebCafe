package com.example.order_service.controller;

import com.example.order_service.entity.Subscription;
import com.example.order_service.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Long userId, @RequestParam(required = false) Subscription.Status status) {
        List<Subscription> subscriptions = subscriptionService.getSubscriptions(userId, status);
        Map<String, Object> response = new HashMap<>();
        response.put("items", subscriptions);
        response.put("total", subscriptions.size());
        return response;
    }

    @PostMapping
    public ResponseEntity<Subscription> create(@RequestBody Subscription subscription) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionService.createSubscription(subscription));
    }

    @PutMapping("/{id}")
    public Subscription update(@PathVariable Long id, @RequestBody Subscription subscription) {
        return subscriptionService.updateSubscription(id, subscription);
    }

    @PostMapping("/{id}/cancel")
    public Subscription cancel(@PathVariable Long id) {
        return subscriptionService.cancelSubscription(id);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
