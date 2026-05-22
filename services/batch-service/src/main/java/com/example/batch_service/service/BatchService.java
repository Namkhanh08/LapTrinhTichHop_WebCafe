package com.example.batch_service.service;

import com.example.batch_service.entity.Batch;
import com.example.batch_service.entity.BatchQualityCheck;
import com.example.batch_service.repository.BatchRepository;
import com.example.batch_service.repository.BatchQualityCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BatchService {

    private final BatchRepository batchRepository;
    private final BatchQualityCheckRepository qualityCheckRepository;

    public BatchService(BatchRepository batchRepository, BatchQualityCheckRepository qualityCheckRepository) {
        this.batchRepository = batchRepository;
        this.qualityCheckRepository = qualityCheckRepository;
    }

    public List<Batch> getAllBatches() {
        return batchRepository.findAll();
    }

    public Batch getBatchById(Long id) {
        return batchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Batch not found with id: " + id));
    }

    public Batch getBatchByCode(String code) {
        return batchRepository.findByBatchCode(code)
            .orElseThrow(() -> new RuntimeException("Batch not found with code: " + code));
    }

    public List<Batch> getBatchesByProduct(String productId) {
        return batchRepository.findByProductId(productId);
    }

    public List<Batch> getBatchesByStatus(String status) {
        return batchRepository.findByStatus(Batch.BatchStatus.valueOf(status));
    }

    public Batch createBatch(Batch batch) {
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        if (batch.getQualityChecks() != null) {
            for (BatchQualityCheck check : batch.getQualityChecks()) {
                check.setBatch(batch);
            }
        }
        return batchRepository.save(batch);
    }

    public Batch updateBatchStatus(Long id, Batch.BatchStatus status) {
        Batch batch = getBatchById(id);
        batch.setStatus(status);
        batch.setUpdatedAt(LocalDateTime.now());
        return batchRepository.save(batch);
    }

    public BatchQualityCheck addQualityCheck(Long batchId, BatchQualityCheck check) {
        Batch batch = getBatchById(batchId);
        check.setBatch(batch);
        check.setCreatedAt(LocalDateTime.now());
        return qualityCheckRepository.save(check);
    }

    public List<BatchQualityCheck> getQualityChecks(Long batchId) {
        return qualityCheckRepository.findByBatchId(batchId);
    }

    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }
}

