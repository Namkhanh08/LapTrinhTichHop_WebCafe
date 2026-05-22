package com.example.Java_Backend.Dtos;

import com.example.Java_Backend.entities.Voucher;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class VoucherAdminResponse {
    private PageResponse<Voucher> pagedData;
    private long activeCount;
    private long usedCount;
    private long freeshipCount;

    public VoucherAdminResponse(PageResponse<Voucher> pagedData, long activeCount, long usedCount, long freeshipCount) {
        this.pagedData = pagedData;
        this.activeCount = activeCount;
        this.usedCount = usedCount;
        this.freeshipCount = freeshipCount;
    }
}