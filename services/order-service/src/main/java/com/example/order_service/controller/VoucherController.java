package com.example.order_service.controller;

import com.example.order_service.entity.Voucher;
import com.example.order_service.service.VoucherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public Map<String, Object> getAllVouchers() {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        long activeCount = vouchers.stream().filter(v -> Boolean.TRUE.equals(v.getIsActive())).count();
        return Map.of(
            "voucher", vouchers,
            "items", vouchers,
            "total", vouchers.size(),
            "activeCount", activeCount,
            "usedTodayCount", 0,
            "freeshipCount", 0
        );
    }

    @GetMapping("/{id}")
    public Voucher getVoucherById(@PathVariable Long id) {
        return voucherService.getVoucherById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Voucher createVoucher(@RequestBody Voucher voucher) {
        return voucherService.createVoucher(voucher);
    }

    @GetMapping("/public")
    public List<Voucher> getPublicVouchers() {
        return voucherService.getPublicVouchers();
    }

    @PostMapping("/available")
    public List<Voucher> getAvailableVouchers(@RequestBody(required = false) Map<String, Object> body) {
        return voucherService.getPublicVouchers();
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyVoucher(@RequestBody Map<String, Object> body) {
        try {
            String code = (String) body.get("code");
            BigDecimal orderAmount = new BigDecimal(body.get("orderAmount").toString());
            
            BigDecimal discount = voucherService.calculateDiscount(code, orderAmount);
            
            return ResponseEntity.ok(Map.of(
                "code", code,
                "discountAmount", discount,
                "success", true
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage(),
                "success", false
            ));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
    }

    @PutMapping("/{id}")
    public Voucher updateVoucher(@PathVariable Long id, @RequestBody Voucher voucher) {
        return voucherService.updateVoucher(id, voucher);
    }

    @PatchMapping("/{id}/toggle")
    public Voucher toggleVoucher(
            @PathVariable Long id,
            @RequestParam(value = "active", required = false) Boolean active) {
        return voucherService.toggleVoucher(id, active);
    }
}
