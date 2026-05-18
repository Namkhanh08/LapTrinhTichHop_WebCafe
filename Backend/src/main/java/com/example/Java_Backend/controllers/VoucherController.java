package com.example.Java_Backend.controllers;

import com.example.Java_Backend.Dtos.VoucherDashboardResponse;
import com.example.Java_Backend.Dtos.CreateVoucherRequest;
import com.example.Java_Backend.services.VoucherService;
import com.example.Java_Backend.Dtos.EligibleVoucherResponse;
import com.example.Java_Backend.Dtos.VoucherEligibilityRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@CrossOrigin(origins = "*")
public class VoucherController {
    @Autowired
    private VoucherService voucherService;

    @GetMapping
    public ResponseEntity<VoucherDashboardResponse> getAllVouchersForDashboard(){
        VoucherDashboardResponse data = voucherService.getVoucherDashboardData();
        return ResponseEntity.ok(data);
    }

    @PostMapping("/available")
    public ResponseEntity<List<EligibleVoucherResponse>> getAvailableVouchers(
            @RequestBody VoucherEligibilityRequest request
    ) {

        List<EligibleVoucherResponse> vouchers =
                voucherService.getEligibleVouchers(request);

        return ResponseEntity.ok(vouchers);
    }

    @GetMapping("/public")
    public ResponseEntity<List<EligibleVoucherResponse>> getPublicVouchers() {

        return ResponseEntity.ok(
                voucherService.getPublicVouchers()
        );
    }

    @PostMapping
    public ResponseEntity<?> createVoucher(
            @RequestBody CreateVoucherRequest request
    ) {

        voucherService.createVoucher(request);

        return ResponseEntity.ok("Tạo voucher thành công");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVoucher(
            @PathVariable Long id,
            @RequestBody CreateVoucherRequest request
    ) {

        voucherService.updateVoucher(id, request);

        return ResponseEntity.ok("Cập nhật voucher thành công");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoucher(
            @PathVariable Long id
    ) {

        voucherService.deleteVoucher(id);

        return ResponseEntity.ok("Xóa voucher thành công");
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleVoucher(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {

        voucherService.toggleVoucher(id, active);

        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }
}
