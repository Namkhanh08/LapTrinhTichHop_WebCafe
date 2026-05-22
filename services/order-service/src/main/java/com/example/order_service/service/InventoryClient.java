package com.example.order_service.service;

import com.example.order_service.entity.OrderItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;

    public InventoryClient(@Value("${inventory.service.url:http://inventory-service:80}") String inventoryServiceUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(inventoryServiceUrl)
            .build();
    }

    public void reserve(OrderItem item, String orderCode) {
        restClient.post()
            .uri("/api/inventory/reserve")
            .body(new StockRequest(item.getProductId(), item.getQuantity(), orderCode))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request, response) -> {
                throw new IllegalStateException("Insufficient inventory for product " + item.getProductId());
            })
            .toBodilessEntity();
    }

    public void release(OrderItem item, String orderCode) {
        restClient.post()
            .uri("/api/inventory/release")
            .body(new StockRequest(item.getProductId(), item.getQuantity(), orderCode))
            .retrieve()
            .toBodilessEntity();
    }

    public void commit(OrderItem item, String orderCode) {
        restClient.post()
            .uri("/api/inventory/commit")
            .body(new StockRequest(item.getProductId(), item.getQuantity(), orderCode))
            .retrieve()
            .toBodilessEntity();
    }

    private record StockRequest(String productId, Integer quantity, String orderId) {
    }
}
