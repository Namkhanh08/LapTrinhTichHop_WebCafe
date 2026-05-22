package com.example.Java_Backend.Dtos;

import com.example.Java_Backend.entities.Voucher;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
public class VoucherDashboardResponse {
    private List<Voucher> voucher;

    private long activeCount;

    private long usedTodayCount;

    private long freeshipCount;

    public VoucherDashboardResponse(List<Voucher> vouchers, long activeCount, long usedTodayCount, long freeshipCount) {
        this.voucher = vouchers;
        this.activeCount = activeCount;
        this.usedTodayCount = usedTodayCount;
        this.freeshipCount = freeshipCount;
    }

}
