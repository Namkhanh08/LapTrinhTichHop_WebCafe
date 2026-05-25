package com.example.Java_Backend.repositories;

import com.example.Java_Backend.entities.Subscription;
import com.example.Java_Backend.entities.SubscriptionConfig;
import com.example.Java_Backend.entities.SubscriptionOrder;
import com.example.Java_Backend.Dtos.SubscriptionResponseDto;
import com.example.Java_Backend.Dtos.UpdateSubscriptionConfigRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SubscriptionRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Tạo gói Subscription mới và trả về Id tự tăng (Identity) để dùng cho bảng Config/Order
    public int createSubscription(Subscription s) {
        String sql = """
            INSERT INTO Subscriptions (UserId, Frequency, DeliveryDay, Status, NextDeliveryDate, CreatedAt, CommitmentMonths, CommitmentEndDate)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getUserId());
            ps.setString(2, s.getFrequency());
            ps.setString(3, s.getDeliveryDay());
            ps.setString(4, s.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(s.getNextDeliveryDate()));
            ps.setTimestamp(6, Timestamp.valueOf(s.getCreatedAt()));
            ps.setInt(7, s.getCommitmentMonths());
            ps.setTimestamp(8, s.getCommitmentEndDate() != null ? Timestamp.valueOf(s.getCommitmentEndDate()) : null);
            return ps;
        }, keyHolder);

        return keyHolder.getKey() != null ? keyHolder.getKey().intValue() : 0;
    }

    // Thêm cấu hình sản phẩm cho gói Subscription
    public void addSubscriptionConfig(SubscriptionConfig config) {
        String sql = """
            INSERT INTO SubscriptionConfigs (SubscriptionId, ProductId, FlavorNote, GrindType, Weight, Quantity)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                config.getSubscriptionId(),
                config.getProductId(),
                config.getFlavorNote(),
                config.getGrindType(),
                config.getWeight(),
                config.getQuantity()
        );
    }

    // Tạo hóa đơn/đơn giao hàng định kỳ đầu tiên
    public void createSubscriptionOrder(SubscriptionOrder order) {
        String sql = """
            INSERT INTO SubscriptionOrders (SubscriptionId, DeliveryDate, SnapshotDetails, Status, CreatedAt, ReceiverName, ReceiverPhone, ShippingAddress, PaymentMethod, FinalPrice)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql,
                order.getSubscriptionId(),
                Timestamp.valueOf(order.getDeliveryDate()),
                order.getSnapshotDetails(),
                order.getStatus(),
                Timestamp.valueOf(order.getCreatedAt()),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getShippingAddress(),
                order.getPaymentMethod(),
                order.getFinalPrice()
        );
    }

    // Tìm kiếm gói Subscription đang kích hoạt của User dựa trên tần suất và ngày giao
    public Subscription findActiveSubscription(String userId, String frequency, String deliveryDay) {
        String sql = """
            SELECT * FROM Subscriptions 
            WHERE UserId = ? AND Frequency = ? AND DeliveryDay = ? AND Status = 'ACTIVE'
        """;
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                Subscription s = new Subscription();
                s.setId(rs.getInt("Id"));
                s.setUserId(rs.getString("UserId"));
                s.setFrequency(rs.getString("Frequency"));
                s.setDeliveryDay(rs.getNString("DeliveryDay"));
                s.setStatus(rs.getString("Status"));
                s.setNextDeliveryDate(rs.getTimestamp("NextDeliveryDate").toLocalDateTime());
                s.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                s.setCommitmentMonths(rs.getInt("CommitmentMonths"));
                s.setCommitmentEndDate(rs.getTimestamp("CommitmentEndDate") != null ? rs.getTimestamp("CommitmentEndDate").toLocalDateTime() : null);
                return s;
            }
            return null;
        }, userId, frequency, deliveryDay);
    }

    // Tìm kiếm cấu hình hạt xem đã tồn tại trong gói chưa (Xử lý chu đáo trường hợp FlavorNote bị NULL y hệt mẫu Cart)
    public SubscriptionConfig findConfigByProductAndSpecs(int subscriptionId, int productId, String flavorNote, Integer grindType, String weight) {
        String normalizedFlavorNote = (flavorNote == null || flavorNote.trim().isEmpty()) ? null : flavorNote.trim();
        String sql = """
            SELECT * FROM SubscriptionConfigs
            WHERE SubscriptionId = ?
              AND ProductId = ?
              AND (
                    (? IS NULL AND FlavorNote IS NULL)
                    OR FlavorNote = ?
                  )
              AND GrindType = ?
              AND Weight = ?
        """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                SubscriptionConfig sc = new SubscriptionConfig();
                sc.setId(rs.getInt("Id"));
                sc.setSubscriptionId(rs.getInt("SubscriptionId"));
                sc.setProductId(rs.getInt("ProductId"));
                sc.setFlavorNote(rs.getString("FlavorNote"));
                sc.setGrindType(rs.getInt("GrindType"));
                sc.setWeight(rs.getString("Weight"));
                sc.setQuantity(rs.getInt("Quantity"));
                return sc;
            }
            return null;
        }, subscriptionId, productId, normalizedFlavorNote, normalizedFlavorNote, grindType, weight);
    }

    // Cập nhật tăng số lượng cấu hình nếu hạt đó đã nằm trong gói
    public void updateConfigQuantity(int configId, int newQuantity) {
        String sql = "UPDATE SubscriptionConfigs SET Quantity = ? WHERE Id = ?";
        jdbcTemplate.update(sql, newQuantity, configId);
    }

    public List<SubscriptionResponseDto> getUserSubscriptions(String userId) {

        String sql = """
        SELECT 
            s.Id,
            p.Name as ProductName,
            s.Frequency,
            s.Status,
            s.NextDeliveryDate,
            sc.FlavorNote,
            sc.GrindType,
            sc.Weight,
            sc.Quantity,
            so.FinalPrice
        FROM Subscriptions s
        INNER JOIN SubscriptionConfigs sc ON sc.SubscriptionId = s.Id
        INNER JOIN Products p ON p.Id = sc.ProductId
        LEFT JOIN SubscriptionOrders so ON so.SubscriptionId = s.Id
        WHERE s.UserId = ?
        ORDER BY s.CreatedAt DESC
    """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            SubscriptionResponseDto dto = new SubscriptionResponseDto();

            dto.setId(rs.getInt("Id"));
            dto.setProductName(rs.getString("ProductName"));
            dto.setFrequency(rs.getString("Frequency"));
            dto.setStatus(rs.getString("Status"));

            dto.setFlavor(rs.getString("FlavorNote"));
            dto.setGrindType(String.valueOf(rs.getInt("GrindType")));
            dto.setWeight(rs.getString("Weight"));
            dto.setQuantity(rs.getInt("Quantity"));

            dto.setPrice(rs.getBigDecimal("FinalPrice"));

            dto.setNextBilling(
                    rs.getTimestamp("NextDeliveryDate") != null
                            ? rs.getTimestamp("NextDeliveryDate").toLocalDateTime().toString()
                            : "---"
            );

            dto.setDeliveryStep(
                    "ACTIVE".equalsIgnoreCase(dto.getStatus()) ? 2 :
                            "SKIPPED".equalsIgnoreCase(dto.getStatus()) ? 1 : 0
            );

            dto.setHistory("Subscription created");

            return dto;

        }, userId);
    }

    public void updateSubscriptionStatus(int subscriptionId, String status) {

        String sql = """
        UPDATE Subscriptions
        SET Status = ?
        WHERE Id = ?
    """;

        jdbcTemplate.update(sql, status, subscriptionId);
    }

    public void cancelSubscription(int subscriptionId) {

        String sql = """
        UPDATE Subscriptions
        SET Status = 'CANCELLED'
        WHERE Id = ?
    """;

        jdbcTemplate.update(sql, subscriptionId);
    }

    public void updateSubscriptionConfig(
            int subscriptionId,
            String flavorNote,
            Integer grindType,
            String weight
    ) {

        String sql = """
        UPDATE SubscriptionConfigs
        SET FlavorNote = ?,
            GrindType = ?,
            Weight = ?
        WHERE SubscriptionId = ?
    """;

        jdbcTemplate.update(
                sql,
                flavorNote,
                grindType,
                weight,
                subscriptionId
        );
    }

    public Subscription findById(int id) {

        String sql = """
        SELECT * FROM Subscriptions
        WHERE Id = ?
    """;

        return jdbcTemplate.query(sql, rs -> {

            if (rs.next()) {

                Subscription s = new Subscription();

                s.setId(rs.getInt("Id"));
                s.setUserId(rs.getString("UserId"));
                s.setFrequency(rs.getString("Frequency"));
                s.setDeliveryDay(rs.getString("DeliveryDay"));
                s.setStatus(rs.getString("Status"));

                return s;
            }

            return null;

        }, id);
    }
}