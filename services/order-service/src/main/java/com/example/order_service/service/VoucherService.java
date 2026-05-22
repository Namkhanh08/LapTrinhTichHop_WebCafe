package com.example.order_service.service;

import com.example.order_service.entity.Voucher;
import com.example.order_service.repository.VoucherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public VoucherService(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    public Voucher getVoucherById(Long id) {
        return voucherRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Voucher not found with id: " + id));
    }

    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Voucher not found with code: " + code));
    }

    public Voucher createVoucher(Voucher voucher) {
        if (voucherRepository.findByCode(voucher.getCode()).isPresent()) {
            throw new IllegalArgumentException("Voucher code already exists: " + voucher.getCode());
        }
        voucher.setCreatedAt(LocalDateTime.now());
        voucher.setUsedCount(0);
        return voucherRepository.save(voucher);
    }

    public List<Voucher> getPublicVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findAll().stream()
            .filter(v -> Boolean.TRUE.equals(v.getIsActive()))
            .filter(v -> !now.isBefore(v.getStartDate()) && !now.isAfter(v.getEndDate()))
            .toList();
    }

    public Voucher updateVoucher(Long id, Voucher changes) {
        Voucher voucher = getVoucherById(id);
        voucher.setCode(changes.getCode());
        voucher.setTitle(changes.getTitle());
        voucher.setDescription(changes.getDescription());
        voucher.setDiscountType(changes.getDiscountType());
        voucher.setDiscountValue(changes.getDiscountValue());
        voucher.setMaxDiscount(changes.getMaxDiscount());
        voucher.setMinOrderValue(changes.getMinOrderValue());
        voucher.setUsageLimit(changes.getUsageLimit());
        voucher.setPaymentMethod(changes.getPaymentMethod());
        voucher.setStartDate(changes.getStartDate());
        voucher.setEndDate(changes.getEndDate());
        if (changes.getIsActive() != null) {
            voucher.setIsActive(changes.getIsActive());
        }
        return voucherRepository.save(voucher);
    }

    public Voucher toggleVoucher(Long id, Boolean active) {
        Voucher voucher = getVoucherById(id);
        voucher.setIsActive(active != null ? active : !Boolean.TRUE.equals(voucher.getIsActive()));
        return voucherRepository.save(voucher);
    }

    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    /**
     * Xác thực và tính toán số tiền giảm giá của voucher.
     * 
     * @param code Mã giảm giá cần áp dụng
     * @param orderAmount Tổng giá trị ban đầu của đơn hàng
     * @return Số tiền được giảm giá
     */
    public BigDecimal calculateDiscount(String code, BigDecimal orderAmount) {
        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO;
        }

        Voucher voucher = voucherRepository.findByCode(code.trim())
            .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại: " + code));

        // 1. Kiểm tra trạng thái hoạt động
        if (Boolean.FALSE.equals(voucher.getIsActive())) {
            throw new IllegalStateException("Mã giảm giá này đã ngừng kích hoạt");
        }

        // 2. Kiểm tra thời hạn hiệu lực
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate())) {
            throw new IllegalStateException("Chương trình giảm giá này chưa bắt đầu");
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new IllegalStateException("Mã giảm giá này đã hết hạn sử dụng");
        }

        // 3. Kiểm tra số lượt giới hạn
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalStateException("Mã giảm giá này đã hết lượt sử dụng");
        }

        // 4. Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderValue() != null && orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Giá trị đơn hàng tối thiểu phải từ " 
                + voucher.getMinOrderValue() + " để áp dụng mã này");
        }

        // 5. Tính toán số tiền được giảm
        BigDecimal discount = BigDecimal.ZERO;
        if ("percentage".equalsIgnoreCase(voucher.getDiscountType())) {
            // Giảm theo % đơn hàng
            BigDecimal rate = voucher.getDiscountValue().divide(BigDecimal.valueOf(100));
            discount = orderAmount.multiply(rate);
            
            // Giới hạn mức giảm tối đa
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else if ("fixed".equalsIgnoreCase(voucher.getDiscountType())) {
            // Giảm theo số tiền cố định
            discount = voucher.getDiscountValue();
        }

        // Không cho phép số tiền giảm vượt quá tổng đơn hàng
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    /**
     * Tăng số lượng đã dùng của Voucher khi đặt hàng thành công.
     */
    public void incrementUsedCount(String code) {
        if (code == null || code.isBlank()) return;
        voucherRepository.findByCode(code.trim()).ifPresent(v -> {
            v.setUsedCount(v.getUsedCount() + 1);
            voucherRepository.save(v);
        });
    }
}
