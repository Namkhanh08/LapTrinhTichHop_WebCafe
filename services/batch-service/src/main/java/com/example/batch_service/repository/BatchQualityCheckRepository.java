package com.example.batch_service.repository;

import com.example.batch_service.entity.BatchQualityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchQualityCheckRepository extends JpaRepository<BatchQualityCheck, Long> {
    List<BatchQualityCheck> findByBatchId(Long batchId);
}

