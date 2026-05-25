package com.example.order_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient(@Value("${identity.service.url:http://identity-service:80}") String identityServiceUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(identityServiceUrl)
            .build();
    }

    public void addLoyaltyPoints(Long userId, int points, String referenceId) {
        if (userId == null || points <= 0) {
            return;
        }

        restClient.post()
            .uri("/api/auth/users/{id}/loyalty/earn", userId)
            .body(new LoyaltyEarnRequest(points, referenceId))
            .retrieve()
            .toBodilessEntity();
    }

    private record LoyaltyEarnRequest(int points, String referenceId) {
    }
}
