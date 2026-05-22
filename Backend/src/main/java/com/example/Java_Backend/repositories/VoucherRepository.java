package com.example.Java_Backend.repositories;

import com.example.Java_Backend.entities.Voucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
public class VoucherRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- HÀM MỚI: PHÂN TRANG, TÌM KIẾM, LỌC TRẠNG THÁI CHO ADMIN ---
    public List<Voucher> findAllAdminPaged(int page, int pageSize, String searchTerm, String status) {
        int offset = (page - 1) * pageSize;
        String searchPattern = "%" + (searchTerm != null ? searchTerm.trim() : "") + "%";

        StringBuilder sql = new StringBuilder("SELECT * FROM Vouchers WHERE (Code LIKE ? OR Title LIKE ?)");
        List<Object> params = new ArrayList<>();
        params.add(searchPattern);
        params.add(searchPattern);

        // Lọc trạng thái theo tham số truyền lên từ Client
        if ("active".equalsIgnoreCase(status)) {
            sql.append(" AND IsActive = 1");
        } else if ("inactive".equalsIgnoreCase(status)) {
            sql.append(" AND IsActive = 0");
        }

        // Cú pháp phân trang trên SQL Server
        sql.append(" ORDER BY Id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            Voucher v = new Voucher();
            v.setId(rs.getLong("Id"));
            v.setCode(rs.getString("Code"));
            v.setTitle(rs.getString("Title"));
            v.setDiscountType(rs.getString("DiscountType"));
            v.setDiscountValue(rs.getBigDecimal("DiscountValue"));
            v.setMaxDiscount(rs.getBigDecimal("MaxDiscount"));
            v.setMinOrderValue(rs.getBigDecimal("MinOrderValue"));
            v.setUsedCount(rs.getInt("UsedCount"));
            v.setUsageLimit(rs.getInt("UsageLimit"));
            v.setPaymentMethod(rs.getString("PaymentMethod"));

            if (rs.getDate("StartDate") != null) {
                v.setStartDate(rs.getDate("StartDate").toLocalDate());
            }
            if (rs.getDate("EndDate") != null) {
                v.setEndDate(rs.getDate("EndDate").toLocalDate());
            }

            v.setIsActive(rs.getBoolean("IsActive"));
            return v;
        });
    }

    // --- HÀM MỚI: ĐẾM TỔNG SỐ DÒNG SAU KHI ĐÃ LỌC ĐỂ PHÂN TRANG ---
    public int countAllAdmin(String searchTerm, String status) {
        String searchPattern = "%" + (searchTerm != null ? searchTerm.trim() : "") + "%";
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Vouchers WHERE (Code LIKE ? OR Title LIKE ?)");
        List<Object> params = new ArrayList<>();
        params.add(searchPattern);
        params.add(searchPattern);

        if ("active".equalsIgnoreCase(status)) {
            sql.append(" AND IsActive = 1");
        } else if ("inactive".equalsIgnoreCase(status)) {
            sql.append(" AND IsActive = 0");
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN HOẶC ĐỂ PHỤC VỤ LOGIC KHÁC ---
    public List<Voucher> findAllManual() {
        String sql = "SELECT * FROM Vouchers";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Voucher v = new Voucher();
            v.setId(rs.getLong("Id"));
            v.setCode(rs.getString("Code"));
            v.setTitle(rs.getString("Title"));
            v.setDiscountType(rs.getString("DiscountType"));
            v.setDiscountValue(rs.getBigDecimal("DiscountValue"));
            v.setMaxDiscount(rs.getBigDecimal("MaxDiscount"));
            v.setMinOrderValue(rs.getBigDecimal("MinOrderValue"));
            v.setUsedCount(rs.getInt("UsedCount"));
            v.setUsageLimit(rs.getInt("UsageLimit"));
            v.setPaymentMethod(rs.getString("PaymentMethod"));
            if (rs.getDate("StartDate") != null) v.setStartDate(rs.getDate("StartDate").toLocalDate());
            if (rs.getDate("EndDate") != null) v.setEndDate(rs.getDate("EndDate").toLocalDate());
            v.setIsActive(rs.getBoolean("IsActive"));
            return v;
        });
    }

    public long countActive(){
        String sql = "SELECT COUNT(*) FROM Vouchers WHERE IsActive = 1";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    public long countFreeship() {
        String sql = "SELECT COUNT(*) FROM Vouchers WHERE DiscountType = 'shipping'";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    public long countTotalUsed() {
        String sql = "SELECT COALESCE(SUM(UsedCount), 0) FROM Vouchers";
        Long total = jdbcTemplate.queryForObject(sql, Long.class);
        return total != null ? total : 0;
    }

    public List<Voucher> findEligibleVouchers(BigDecimal subtotal, String paymentMethod) {
        String sql = "SELECT * FROM Vouchers WHERE IsActive = 1 AND GETDATE() BETWEEN StartDate AND EndDate AND UsedCount < UsageLimit AND MinOrderValue <= ? AND (PaymentMethod = 'ALL' OR PaymentMethod = ?)";
        return jdbcTemplate.query(sql, new Object[]{subtotal, paymentMethod}, (rs, rowNum) -> {
            Voucher v = new Voucher();
            v.setId(rs.getLong("Id"));
            v.setCode(rs.getString("Code"));
            v.setTitle(rs.getString("Title"));
            v.setDiscountType(rs.getString("DiscountType"));
            v.setDiscountValue(rs.getBigDecimal("DiscountValue"));
            v.setMaxDiscount(rs.getBigDecimal("MaxDiscount"));
            v.setMinOrderValue(rs.getBigDecimal("MinOrderValue"));
            v.setUsedCount(rs.getInt("UsedCount"));
            v.setUsageLimit(rs.getInt("UsageLimit"));
            v.setPaymentMethod(rs.getString("PaymentMethod"));
            v.setIsActive(rs.getBoolean("IsActive"));
            return v;
        });
    }

    public List<Voucher> findPublicVouchers() {
        String sql = "SELECT * FROM Vouchers WHERE IsActive = 1 AND GETDATE() BETWEEN StartDate AND EndDate AND UsedCount < UsageLimit ORDER BY DiscountValue DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Voucher v = new Voucher();
            v.setId(rs.getLong("Id"));
            v.setCode(rs.getString("Code"));
            v.setTitle(rs.getString("Title"));
            v.setDiscountType(rs.getString("DiscountType"));
            v.setDiscountValue(rs.getBigDecimal("DiscountValue"));
            v.setMaxDiscount(rs.getBigDecimal("MaxDiscount"));
            v.setMinOrderValue(rs.getBigDecimal("MinOrderValue"));
            v.setUsedCount(rs.getInt("UsedCount"));
            v.setUsageLimit(rs.getInt("UsageLimit"));
            v.setPaymentMethod(rs.getString("PaymentMethod"));
            v.setIsActive(rs.getBoolean("IsActive"));
            return v;
        });
    }

    public int createVoucher(Voucher v) {
        String sql = "INSERT INTO Vouchers (Code, Title, DiscountType, DiscountValue, MaxDiscount, MinOrderValue, UsedCount, UsageLimit, PaymentMethod, StartDate, EndDate, IsActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, v.getCode(), v.getTitle(), v.getDiscountType(), v.getDiscountValue(), v.getMaxDiscount(), v.getMinOrderValue(), 0, v.getUsageLimit(), v.getPaymentMethod(), v.getStartDate(), v.getEndDate(), v.getIsActive());
    }

    public int updateVoucher(Long id, Voucher v) {
        String sql = "UPDATE Vouchers SET Code = ?, Title = ?, DiscountType = ?, DiscountValue = ?, MaxDiscount = ?, MinOrderValue = ?, UsageLimit = ?, PaymentMethod = ?, StartDate = ?, EndDate = ?, IsActive = ? WHERE Id = ?";
        return jdbcTemplate.update(sql, v.getCode(), v.getTitle(), v.getDiscountType(), v.getDiscountValue(), v.getMaxDiscount(), v.getMinOrderValue(), v.getUsageLimit(), v.getPaymentMethod(), v.getStartDate(), v.getEndDate(), v.getIsActive(), id);
    }

    public int deleteVoucher(Long id) {
        String sql = "DELETE FROM Vouchers WHERE Id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int toggleVoucher(Long id, boolean active) {
        String sql = "UPDATE Vouchers SET IsActive = ? WHERE Id = ?";
        return jdbcTemplate.update(sql, active, id);
    }
}