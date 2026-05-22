package com.example.order_service.service;

import com.example.order_service.entity.Subscription;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final OrderService orderService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, OrderService orderService) {
        this.subscriptionRepository = subscriptionRepository;
        this.orderService = orderService;
    }

    public List<Subscription> getSubscriptions(Long userId, Subscription.Status status) {
        if (userId == null && status == null) {
            return subscriptionRepository.findAllByOrderByCreatedAtDesc();
        }
        if (userId == null) {
            return subscriptionRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        if (status != null) {
            return subscriptionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        }
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Subscription createSubscription(Subscription subscription) {
        validate(subscription);
        subscription.setStatus(Subscription.Status.active);
        subscription.setNextDeliveryFromFrequency();
        Subscription saved = subscriptionRepository.save(subscription);
        createInitialOrder(saved);
        return saved;
    }

    public Subscription updateSubscription(Long id, Subscription update) {
        Subscription subscription = getSubscription(id);

        if (update.getProductId() != null && !update.getProductId().isBlank()) {
            subscription.setProductId(update.getProductId());
        }
        if (update.getGrindSizeId() != null) {
            subscription.setGrindSizeId(update.getGrindSizeId());
        }
        if (update.getFrequency() != null) {
            subscription.setFrequency(update.getFrequency());
            subscription.setNextDeliveryFromFrequency();
        }
        if (update.getQuantity() != null) {
            if (update.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            subscription.setQuantity(update.getQuantity());
        }

        return subscriptionRepository.save(subscription);
    }

    public Subscription cancelSubscription(Long id) {
        Subscription subscription = getSubscription(id);

        if (subscription.getNextDeliveryDate() != null
            && !subscription.getNextDeliveryDate().isAfter(LocalDate.now().plusDays(1))) {
            throw new IllegalStateException("Cannot cancel subscription within 24 hours before delivery");
        }

        subscription.setStatus(Subscription.Status.cancelled);
        return subscriptionRepository.save(subscription);
    }

    private Subscription getSubscription(Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
    }

    private void validate(Subscription subscription) {
        if (subscription.getUserId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (subscription.getProductId() == null || subscription.getProductId().isBlank()) {
            throw new IllegalArgumentException("Product is required");
        }
        if (subscription.getFrequency() == null) {
            throw new IllegalArgumentException("Frequency is required");
        }
        if (subscription.getQuantity() == null || subscription.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        if (subscription.getUnitPrice() == null || subscription.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be zero or greater");
        }
    }

    private void createInitialOrder(Subscription subscription) {
        Order order = new Order();
        order.setUserId(subscription.getUserId());
        order.setSubscriptionId(subscription.getId());
        order.setUserEmail(subscription.getUserEmail());
        order.setUserName(subscription.getUserName());
        order.setPaymentMethod("subscription");
        order.setNotes("Initial order generated from subscription #" + subscription.getId());

        OrderItem item = new OrderItem();
        item.setProductId(subscription.getProductId());
        item.setProductName(subscription.getProductName());
        item.setQuantity(subscription.getQuantity());
        item.setUnitPrice(subscription.getUnitPrice());
        order.setItems(List.of(item));

        orderService.createOrder(order);
    }
}
