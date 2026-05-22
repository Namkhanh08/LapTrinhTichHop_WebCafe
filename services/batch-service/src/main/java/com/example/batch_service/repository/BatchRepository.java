package com.example.batch_service.repository;

import com.example.batch_service.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    Optional<Batch> findByBatchCode(String batchCode);
    List<Batch> findByProductId(String productId);
    List<Batch> findByStatus(Batch.BatchStatus status);
}

