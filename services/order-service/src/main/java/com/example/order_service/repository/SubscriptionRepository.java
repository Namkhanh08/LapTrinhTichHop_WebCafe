package com.example.order_service.repository;

import com.example.order_service.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByOrderByCreatedAtDesc();
    List<Subscription> findByStatusOrderByCreatedAtDesc(Subscription.Status status);
    List<Subscription> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Subscription> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Subscription.Status status);
}
