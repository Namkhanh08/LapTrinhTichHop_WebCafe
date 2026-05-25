package com.example.Java_Backend.services;

import com.example.Java_Backend.Dtos.CreateSubscriptionRequest;
import com.example.Java_Backend.Dtos.SubscriptionResponseDto;
import com.example.Java_Backend.Dtos.UpdateSubscriptionConfigRequest;
import com.example.Java_Backend.entities.Subscription;
import com.example.Java_Backend.entities.SubscriptionConfig;
import com.example.Java_Backend.entities.SubscriptionOrder;
import com.example.Java_Backend.repositories.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository _subscriptionRepo;

    @Transactional
    public void createSubscription(String userId, CreateSubscriptionRequest request) {
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be greater than zero!");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra xem User đã có gói đăng ký nào cùng chu kỳ & ngày giao đang ACTIVE chưa
        Subscription sub = _subscriptionRepo.findActiveSubscription(userId, request.getFrequency(), request.getDeliveryDay());
        int subscriptionId;

        // Tính toán ngày giao hàng đầu tiên thực tế sắp tới (ví dụ: Thứ Bảy này hoặc Thứ Ba tới)
        LocalDateTime firstDeliveryDate = calculateFirstDeliveryDate(request.getDeliveryDay());

        if (sub == null) {
            // Tính toán số tháng cam kết
            int commitmentMonths = 0;
            if ("3months".equalsIgnoreCase(request.getCommitment())) {
                commitmentMonths = 3;
            } else if ("6months".equalsIgnoreCase(request.getCommitment())) {
                commitmentMonths = 6;
            }

            // Tính toán ngày giao hàng của chu kỳ tiếp theo (kỳ số 2) dựa theo tần suất khách chọn
            LocalDateTime nextDeliveryDate = calculateNextDeliveryDate(firstDeliveryDate, request.getFrequency());

            Subscription newSub = new Subscription();
            newSub.setUserId(userId);
            newSub.setFrequency(request.getFrequency());
            newSub.setDeliveryDay(request.getDeliveryDay());
            newSub.setStatus("ACTIVE");
            newSub.setCreatedAt(now);

            // ĐÃ FIX: Không còn hardcode 4 ngày, dùng ngày tính toán thực tế
            newSub.setNextDeliveryDate(nextDeliveryDate);

            newSub.setCommitmentMonths(commitmentMonths);
            newSub.setCommitmentEndDate(commitmentMonths > 0 ? now.plusMonths(commitmentMonths) : null);

            // Lưu gói cha và nhận về ID tự tăng vừa tạo dưới DB
            subscriptionId = _subscriptionRepo.createSubscription(newSub);
        } else {
            subscriptionId = sub.getId();
        }

        // 2. Xử lý cấu hình chi tiết sản phẩm hạt (SubscriptionConfigs)
        SubscriptionConfig existingConfig = _subscriptionRepo.findConfigByProductAndSpecs(
                subscriptionId,
                request.getProductId(),
                request.getFlavorNotes(),
                request.getGrindType(),
                request.getWeight()
        );

        if (existingConfig != null) {
            int newQuantity = existingConfig.getQuantity() + request.getQuantity();
            _subscriptionRepo.updateConfigQuantity(existingConfig.getId(), newQuantity);
        } else {
            SubscriptionConfig config = new SubscriptionConfig();
            config.setSubscriptionId(subscriptionId);
            config.setProductId(request.getProductId());
            config.setFlavorNote(request.getFlavorNotes());
            config.setGrindType(request.getGrindType());
            config.setWeight(request.getWeight());
            config.setQuantity(request.getQuantity());

            _subscriptionRepo.addSubscriptionConfig(config);
        }

        // 3. Tạo hóa đơn/đơn giao hàng đầu tiên (SubscriptionOrders)
        SubscriptionOrder order = new SubscriptionOrder();
        order.setSubscriptionId(subscriptionId);

        // ĐÃ FIX: Đơn hàng đầu tiên sẽ đi vào ngày giao hàng đầu tiên tìm được
        order.setDeliveryDate(firstDeliveryDate);

        order.setStatus("PENDING_PAYMENT");
        order.setCreatedAt(now);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setFinalPrice(request.getTotalPrice());

        // CHƠI MAP THỦ CÔNG: Tạo chuỗi JSON snapshot thông tin sản phẩm
        String jsonSnapshot = String.format(
                "{\"ProductId\": %d, \"FlavorNotes\": \"%s\", \"GrindType\": \"%s\", \"Weight\": \"%s\", \"Quantity\": %d, \"FinalPrice\": %s}",
                request.getProductId(),
                request.getFlavorNotes() != null ? request.getFlavorNotes().replace("\"", "\\\"") : "",
                request.getGrindType() != null ? request.getGrindType() : "",
                request.getWeight() != null ? request.getWeight().replace("\"", "\\\"") : "",
                request.getQuantity(),
                request.getTotalPrice() != null ? request.getTotalPrice().toString() : "0"
        );

        order.setSnapshotDetails(jsonSnapshot);

        _subscriptionRepo.createSubscriptionOrder(order);
    }

    /**
     * Hàm bổ trợ: Tìm ngày có thứ mong muốn (Thứ Ba, Thứ Bảy...) ĐẦU TIÊN tính từ thời điểm hiện tại
     */
    private LocalDateTime calculateFirstDeliveryDate(String deliveryDayStr) {
        LocalDateTime target = LocalDateTime.now();
        DayOfWeek targetDay;

        // Chuẩn hóa chuỗi tiếng Việt / tiếng Anh từ Front-end gửi lên sang DayOfWeek của Java
        switch (deliveryDayStr.trim().toLowerCase()) {
            case "thứ hai": case "thứ 2": case "monday": targetDay = DayOfWeek.MONDAY; break;
            case "thứ ba": case "thứ 3": case "tuesday": targetDay = DayOfWeek.TUESDAY; break;
            case "thứ tư": case "thứ 4": case "wednesday": targetDay = DayOfWeek.WEDNESDAY; break;
            case "thứ năm": case "thứ 5": case "thursday": targetDay = DayOfWeek.THURSDAY; break;
            case "thứ sáu": case "thứ 6": case "friday": targetDay = DayOfWeek.FRIDAY; break;
            case "thứ bảy": case "thứ 7": case "saturday": targetDay = DayOfWeek.SATURDAY; break;
            default: targetDay = DayOfWeek.SUNDAY;
        }

        // Vòng lặp cộng tịnh tiến từng ngày cho đến khi khớp với Thứ mong muốn
        // Nếu trùng ngày hôm nay thì mặc định giao luôn hoặc bạn có thể đổi thành target.plusDays(1) tùy nhu cầu mẻ rang
        while (target.getDayOfWeek() != targetDay) {
            target = target.plusDays(1);
        }
        return target;
    }

    /**
     * Hàm bổ trợ: Tính toán ngày giao kế tiếp (kỳ sau) dựa trên ngày gốc và tần suất (Frequency)
     */
    private LocalDateTime calculateNextDeliveryDate(LocalDateTime firstDeliveryDate, String frequency) {
        // Kiểm tra nếu chu kỳ chọn là 2 tuần một lần
        if ("2weeks".equalsIgnoreCase(frequency) || "2 tuần / 1 lần".equalsIgnoreCase(frequency)) {
            return firstDeliveryDate.plusWeeks(2);
        }
        // Kiểm tra nếu chu kỳ chọn là 4 tuần một lần
        if ("4weeks".equalsIgnoreCase(frequency) || "4 tuần / 1 lần".equalsIgnoreCase(frequency)) {
            return firstDeliveryDate.plusWeeks(4);
        }
        // Mặc định hoặc chu kỳ ngắn nhất: 1 tuần một lần
        return firstDeliveryDate.plusWeeks(1);
    }

    public List<SubscriptionResponseDto> getUserSubscriptions(String userId) {
        return _subscriptionRepo.getUserSubscriptions(userId);
    }

    @Transactional
    public void toggleSkipSubscription(int subscriptionId) {

        Subscription sub = _subscriptionRepo.findById(subscriptionId);

        if (sub == null) {
            throw new RuntimeException("Subscription not found");
        }

        String nextStatus =
                "ACTIVE".equalsIgnoreCase(sub.getStatus())
                        ? "SKIPPED"
                        : "ACTIVE";

        _subscriptionRepo.updateSubscriptionStatus(subscriptionId, nextStatus);
    }

    @Transactional
    public void cancelSubscription(int subscriptionId) {

        Subscription sub = _subscriptionRepo.findById(subscriptionId);

        if (sub == null) {
            throw new RuntimeException("Subscription not found");
        }

        _subscriptionRepo.cancelSubscription(subscriptionId);
    }

    @Transactional
    public void updateSubscriptionConfig(
            int subscriptionId,
            UpdateSubscriptionConfigRequest request
    ) {

        _subscriptionRepo.updateSubscriptionConfig(
                subscriptionId,
                request.getFlavorNote(),
                request.getGrindType(),
                request.getWeight()
        );
    }
}