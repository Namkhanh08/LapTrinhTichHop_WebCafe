package com.example.Java_Backend.services;

import com.example.Java_Backend.Dtos.*;
import com.example.Java_Backend.entities.Voucher;
import com.example.Java_Backend.repositories.VoucherRepository;
import com.example.Java_Backend.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepo;

    @Autowired
    private ProductRepository productRepo;

    // --- HÀM XỬ LÝ PHÂN TRANG VÀ THỐNG KÊ MỚI CHO ADMIN ---
    public VoucherAdminResponse getVouchersAdminPaged(int page, String searchTerm, String status) {
        int pageSize = 10; // Quy định hiển thị 10 bản ghi trên trang

        // 1. Lấy danh sách voucher đã phân trang, tìm kiếm và lọc dưới DB
        List<Voucher> items = voucherRepo.findAllAdminPaged(page, pageSize, searchTerm, status);

        // 2. Đếm tổng số lượng bản ghi thỏa mãn điều kiện lọc
        int totalItems = voucherRepo.countAllAdmin(searchTerm, status);

        // 3. Đóng gói vào PageResponse dùng chung của bạn
        PageResponse<Voucher> pagedData = new PageResponse<>(items, totalItems, page, pageSize);

        // 4. Lấy dữ liệu thống kê tổng quan bất biến (phục vụ 3 ô Thống kê đầu trang)
        long active = voucherRepo.countActive();
        long freeship = voucherRepo.countFreeship();
        long totalUsed = voucherRepo.countTotalUsed();

        // 5. Trả về thực thể bọc ngoài đầy đủ
        return new VoucherAdminResponse(pagedData, active, totalUsed, freeship);
    }

    // --- CÁC LOGIC KHÁC GIỮ NGUYÊN CHUẨN ĐẦU RA ---
    public VoucherDashboardResponse getVoucherDashboardData() {
        List<Voucher> voucher = voucherRepo.findAllManual();
        long active = voucherRepo.countActive();
        long freeship = voucherRepo.countFreeship();
        long totalUsed = voucherRepo.countTotalUsed();
        return new VoucherDashboardResponse(voucher, active, totalUsed, freeship);
    }

    public List<EligibleVoucherResponse> getEligibleVouchers(VoucherEligibilityRequest request) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemDto item : request.getItems()) {
            BigDecimal productPrice = productRepo.getProductPrice(item.getProductId());
            BigDecimal itemTotal = productPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        List<Voucher> vouchers = voucherRepo.findEligibleVouchers(subtotal, request.getPaymentMethod());
        final BigDecimal finalSubtotal = subtotal;

        return vouchers.stream().map(v -> {
            EligibleVoucherResponse dto = new EligibleVoucherResponse();
            dto.setId(v.getId());
            dto.setCode(v.getCode());
            dto.setTitle(v.getTitle());
            dto.setDiscountType(v.getDiscountType());
            BigDecimal preview = BigDecimal.ZERO;

            if ("percent".equals(v.getDiscountType())) {
                preview = finalSubtotal.multiply(v.getDiscountValue().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                if (v.getMaxDiscount() != null && preview.compareTo(v.getMaxDiscount()) > 0) {
                    preview = v.getMaxDiscount();
                }
            } else if ("fixed".equals(v.getDiscountType()) || "shipping".equals(v.getDiscountType())) {
                preview = v.getDiscountValue();
            }
            dto.setDiscountPreview(preview);
            return dto;
        }).toList();
    }

    public List<EligibleVoucherResponse> getPublicVouchers() {
        List<Voucher> vouchers = voucherRepo.findPublicVouchers();
        return vouchers.stream().map(v -> {
            EligibleVoucherResponse dto = new EligibleVoucherResponse();
            dto.setId(v.getId());
            dto.setCode(v.getCode());
            dto.setTitle(v.getTitle());
            dto.setDiscountType(v.getDiscountType());
            dto.setDiscountPreview(v.getDiscountValue());
            return dto;
        }).toList();
    }

    public void createVoucher(CreateVoucherRequest request) {
        Voucher v = new Voucher();
        v.setCode(request.getCode());
        v.setTitle(request.getTitle());
        v.setDiscountType(request.getDiscountType());
        v.setDiscountValue(request.getDiscountValue());
        v.setMaxDiscount(request.getMaxDiscount());
        v.setMinOrderValue(request.getMinOrderValue());
        v.setUsageLimit(request.getUsageLimit());
        v.setPaymentMethod(request.getPaymentMethod());
        v.setStartDate(request.getStartDate());
        v.setEndDate(request.getEndDate());
        v.setIsActive(request.getIsActive());
        voucherRepo.createVoucher(v);
    }

    public void updateVoucher(Long id, CreateVoucherRequest request) {
        Voucher v = new Voucher();
        v.setCode(request.getCode());
        v.setTitle(request.getTitle());
        v.setDiscountType(request.getDiscountType());
        v.setDiscountValue(request.getDiscountValue());
        v.setMaxDiscount(request.getMaxDiscount());
        v.setMinOrderValue(request.getMinOrderValue());
        v.setUsageLimit(request.getUsageLimit());
        v.setPaymentMethod(request.getPaymentMethod());
        v.setStartDate(request.getStartDate());
        v.setEndDate(request.getEndDate());
        v.setIsActive(request.getIsActive());
        voucherRepo.updateVoucher(id, v);
    }

    public void deleteVoucher(Long id) {
        voucherRepo.deleteVoucher(id);
    }

    public void toggleVoucher(Long id, boolean active) {
        voucherRepo.toggleVoucher(id, active);
    }
}