package com.example.batch_service.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_code", nullable = false, unique = true)
    private String batchCode;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "package_250g_count")
    private Integer package250gCount = 0;

    @Column(name = "package_500g_count")
    private Integer package500gCount = 0;

    @Column(name = "package_1000g_count")
    private Integer package1000gCount = 0;

    @Column(name = "roast_date", nullable = false)
    private LocalDate roastDate;

    @Column(name = "roast_level")
    private String roastLevel;

    @Column(name = "origin_region")
    private String originRegion;

    @Column(name = "process_method")
    private String processMethod;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status = BatchStatus.roasting;

    @Column(name = "created_by")
    private String createdBy;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BatchQualityCheck> qualityChecks = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPackage250gCount() { return package250gCount; }
    public void setPackage250gCount(Integer package250gCount) { this.package250gCount = package250gCount; }

    public Integer getPackage500gCount() { return package500gCount; }
    public void setPackage500gCount(Integer package500gCount) { this.package500gCount = package500gCount; }

    public Integer getPackage1000gCount() { return package1000gCount; }
    public void setPackage1000gCount(Integer package1000gCount) { this.package1000gCount = package1000gCount; }

    public LocalDate getRoastDate() { return roastDate; }
    public void setRoastDate(LocalDate roastDate) { this.roastDate = roastDate; }

    public String getRoastLevel() { return roastLevel; }
    public void setRoastLevel(String roastLevel) { this.roastLevel = roastLevel; }

    public String getOriginRegion() { return originRegion; }
    public void setOriginRegion(String originRegion) { this.originRegion = originRegion; }

    public String getProcessMethod() { return processMethod; }
    public void setProcessMethod(String processMethod) { this.processMethod = processMethod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<BatchQualityCheck> getQualityChecks() { return qualityChecks; }
    public void setQualityChecks(List<BatchQualityCheck> qualityChecks) { this.qualityChecks = qualityChecks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum BatchStatus {
        roasting, cooling, quality_check, packaging, completed, rejected
    }
}

