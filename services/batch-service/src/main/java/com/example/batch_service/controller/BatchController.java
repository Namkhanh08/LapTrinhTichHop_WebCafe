package com.example.batch_service.controller;

import com.example.batch_service.entity.Batch;
import com.example.batch_service.entity.BatchQualityCheck;
import com.example.batch_service.service.BatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBatches(
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String status) {

        List<Batch> batches;
        if (productId != null) {
            batches = batchService.getBatchesByProduct(productId);
        } else if (status != null) {
            batches = batchService.getBatchesByStatus(status);
        } else {
            batches = batchService.getAllBatches();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("items", batches);
        response.put("total", batches.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Batch> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Batch> getBatchByCode(@PathVariable String code) {
        return ResponseEntity.ok(batchService.getBatchByCode(code));
    }

    @PostMapping
    public ResponseEntity<Batch> createBatch(@RequestBody Batch batch) {
        Batch created = batchService.createBatch(batch);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Batch> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Batch.BatchStatus status = Batch.BatchStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(batchService.updateBatchStatus(id, status));
    }

    @PostMapping("/{id}/quality-checks")
    public ResponseEntity<BatchQualityCheck> addQualityCheck(@PathVariable Long id, @RequestBody BatchQualityCheck check) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.addQualityCheck(id, check));
    }

    @GetMapping("/{id}/quality-checks")
    public ResponseEntity<List<BatchQualityCheck>> getQualityChecks(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getQualityChecks(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        batchService.deleteBatch(id);
        return ResponseEntity.noContent().build();
    }
}

