package com.example.batch_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_quality_checks")
public class BatchQualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnore
    private Batch batch;

    @Column(name = "check_date", nullable = false)
    private LocalDate checkDate;

    @Column(name = "moisture_content", precision = 5, scale = 2)
    private BigDecimal moistureContent;

    @Column(name = "bean_density", precision = 6, scale = 2)
    private BigDecimal beanDensity;

    @Column(name = "color_score")
    private Integer colorScore;

    @Column(name = "defect_count")
    private Integer defectCount = 0;

    @Column(name = "aroma_notes", columnDefinition = "TEXT")
    private String aromaNotes;

    @Column(nullable = false)
    private Boolean passed = false;

    @Column(name = "checked_by")
    private String checkedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Batch getBatch() { return batch; }
    public void setBatch(Batch batch) { this.batch = batch; }

    public LocalDate getCheckDate() { return checkDate; }
    public void setCheckDate(LocalDate checkDate) { this.checkDate = checkDate; }

    public BigDecimal getMoistureContent() { return moistureContent; }
    public void setMoistureContent(BigDecimal moistureContent) { this.moistureContent = moistureContent; }

    public BigDecimal getBeanDensity() { return beanDensity; }
    public void setBeanDensity(BigDecimal beanDensity) { this.beanDensity = beanDensity; }

    public Integer getColorScore() { return colorScore; }
    public void setColorScore(Integer colorScore) { this.colorScore = colorScore; }

    public Integer getDefectCount() { return defectCount; }
    public void setDefectCount(Integer defectCount) { this.defectCount = defectCount; }

    public String getAromaNotes() { return aromaNotes; }
    public void setAromaNotes(String aromaNotes) { this.aromaNotes = aromaNotes; }

    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }

    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

