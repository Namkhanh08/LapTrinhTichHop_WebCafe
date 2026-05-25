package com.example.batch_service.service;

import com.example.batch_service.entity.Batch;
import com.example.batch_service.entity.BatchQualityCheck;
import com.example.batch_service.repository.BatchRepository;
import com.example.batch_service.repository.BatchQualityCheckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        validateBatch(batch);
        if (batch.getBatchCode() == null || batch.getBatchCode().isBlank()) {
            batch.setBatchCode(generateBatchCode());
        }
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

    public Batch updatePackaging(Long id, Integer package250gCount, Integer package500gCount, Integer package1000gCount) {
        Batch batch = getBatchById(id);
        int p250 = nonNegative(package250gCount);
        int p500 = nonNegative(package500gCount);
        int p1000 = nonNegative(package1000gCount);
        int packagedGrams = p250 * 250 + p500 * 500 + p1000 * 1000;
        int batchGrams = batch.getQuantity() * 1000;

        if (packagedGrams > batchGrams) {
            throw new IllegalArgumentException("Total packaged quantity exceeds batch yield");
        }

        batch.setPackage250gCount(p250);
        batch.setPackage500gCount(p500);
        batch.setPackage1000gCount(p1000);
        batch.setStatus(Batch.BatchStatus.packaging);
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

    private void validateBatch(Batch batch) {
        if (batch.getProductId() == null || batch.getProductId().isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
        if (batch.getQuantity() == null || batch.getQuantity() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (batch.getRoastDate() == null) {
            throw new IllegalArgumentException("roastDate is required");
        }
        if (batch.getRoastDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("roastDate cannot be in the future");
        }
    }

    private int nonNegative(Integer value) {
        if (value == null) {
            return 0;
        }
        if (value < 0) {
            throw new IllegalArgumentException("package counts must be zero or greater");
        }
        return value;
    }

    private String generateBatchCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        int random = (int) (Math.random() * 1000);
        return "BATCH-" + timestamp + "-" + random;
    }
}

