package com.example.Java_Backend.services;

import com.example.Java_Backend.Dtos.OrderItemDto;
import com.example.Java_Backend.Dtos.CreateOrderDto;
import com.example.Java_Backend.Dtos.UpdateOrderDto;
import com.example.Java_Backend.entities.Order;
import com.example.Java_Backend.entities.OrderDetail;
import com.example.Java_Backend.entities.Product;
import com.example.Java_Backend.repositories.CartRepository;
import com.example.Java_Backend.repositories.OrderRepository;
import com.example.Java_Backend.repositories.ProductRepository;
import com.example.Java_Backend.Dtos.PageResponse;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service

public class OrderService {
    @Autowired
    private OrderRepository oRepo;

    @Autowired
    private ProductRepository pRepo;

    @Autowired
    private CartRepository cRepo;

    public List<Order> getMyOrders (String userId){
        return oRepo.GetByUserAsync(userId);
    }

    public List<Order> getAllOrders(){
        return oRepo.getAll();
    }

    public Order getById(int id){
        Order order = oRepo.getById(id);

        if(order == null){
            throw new RuntimeException("Không tìm thấy đơn hàng!");
        }
        return order;
    }

    private int extractWeightValue(String weightStr){
        if(weightStr == null || weightStr.isEmpty()) return 0;

        String numberOnly = weightStr.replaceAll("[^\\d]", "");
        if(numberOnly.isEmpty()) return 0;

        int value = Integer.parseInt(numberOnly);

        if(weightStr.toLowerCase().contains("kg")){
            return value * 1000;
        }

        return value;
    }

    public Order createOrder(String userId, CreateOrderDto dto){
        double totalAmount = 0;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for(OrderItemDto item : dto.getItems()){
            Product product = pRepo.getById(item.getProductId());

            if(product == null){
                throw new RuntimeException("Không tìm thấy sản phẩm!");
            }

            if(product.getStock() < item.getQuantity()){
                throw new RuntimeException("Số lượng trong kho không đủ để đáp ứng!");
            }

            totalAmount += (product.getPrice() * item.getQuantity());

            OrderDetail detail = new OrderDetail();

            detail.setProductId(item.getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(product.getPrice());
            detail.setFlavorNotes(item.getFlavorNotes());
            detail.setGrindingOptionId(item.getGrindingOptionId());
            detail.setWeight(item.getWeight());

            orderDetails.add(detail);

            int weightPerPackage = extractWeightValue(item.getWeight());
            int totalRemove = weightPerPackage * item.getQuantity();
            if(product.getStock() < totalRemove) throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ để đặt hàng");

            product.setStock(product.getStock() - totalRemove);
            pRepo.Update(product);

            cRepo.deleteItem(item.getProductId(), item.getGrindingOptionId(), item.getFlavorNotes(), item.getWeight());
        }

        Order order = new Order();

        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        if("VNPAY".equalsIgnoreCase(dto.getPaymentMethod())){
            order.setStatus("Chờ thanh toán");
        }else{
            order.setStatus("Chờ xử lý");
        }
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setShippingProvince(dto.getShippingProvince());
        order.setShippingDistrict(dto.getShippingDistrict());
        order.setShippingWard(dto.getShippingWard());
        order.setShippingDetailAddress(dto.getShippingDetailAddress());
        order.setShippingNote(dto.getShippingNote());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setVoucherCode(dto.getVoucherCode());
        order.setDiscountAmount(dto.getDiscountAmount());
        order.setFinalAmount(dto.getFinalAmount());

        order.setOrderDetails(orderDetails);
        oRepo.create(order);;
        return order;
    }

    public Order updateStatus(int id, String status){
        Order order = oRepo.getById(id);

        if(order == null){
            throw new RuntimeException("Không tìm thấy đơn hàng!");
        }
        order.setStatus(status);
        oRepo.update(order);
        return order;
    }

    public void cancelOrder(int id){
        Order order = oRepo.getById(id);

        if(order == null){
            throw new RuntimeException("Không tìm thấy đơn hàng");
        }

        if(
                !order.getStatus().equals("Chờ thanh toán") && !order.getStatus().equals("Chờ xử lý")
        ){
            throw new RuntimeException("Đơn hàng này không thể hủy!");
        }

        List<OrderDetail> details = oRepo.getByOrder(id);

        for(OrderDetail detail : details){
            Product product = pRepo.getById(detail.getProductId());

            if(product != null){
                int weightPerPackage = extractWeightValue(detail.getWeight());
                int totalRemove = weightPerPackage * detail.getQuantity();
                if(product.getStock() < totalRemove) throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ để đặt hàng");


                product.setStock(product.getStock() - totalRemove);

                pRepo.Update(product);
            }
        }

        order.setStatus("Đã hủy");
        oRepo.update(order);
    }

    public Order updateOrder(int id, UpdateOrderDto dto){

        Order order = oRepo.getById(id);

        if(order == null){
            throw new RuntimeException("Không tìm thấy đơn hàng!");
        }

        if(
                !"Chờ xử lý".equals(order.getStatus()) &&
                        !"Chờ thanh toán".equals(order.getStatus()) && !"Đã xác nhận".equals(order.getStatus())
        ){
            throw new RuntimeException("Đơn hàng này không thể chỉnh sửa!");
        }

        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());

        order.setShippingProvince(dto.getShippingProvince());
        order.setShippingDistrict(dto.getShippingDistrict());
        order.setShippingWard(dto.getShippingWard());

        order.setShippingDetailAddress(dto.getShippingDetailAddress());
        order.setShippingNote(dto.getShippingNote());

        order.setPaymentMethod(dto.getPaymentMethod());

        // UPDATE STATUS THEO PAYMENT
        if("VNPAY".equalsIgnoreCase(dto.getPaymentMethod())){
            order.setStatus("Chờ thanh toán");
        }else{
            order.setStatus("Chờ xử lý");
        }

        oRepo.updateFullOrder(order);

        return order;
    }

    @Transactional
    public Order confirmOrder(int id){
        Order order = oRepo.getById(id);
        if(order == null) throw new RuntimeException("Không tìm thấy đơn hàng");
        if(!order.getStatus().equals("Chờ xử lý")) throw new RuntimeException("Chỉ xác nhận đơn ở trạng thái Chờ xử lý");

        //kiểm tra tồn kho lần cuối trước khi tiến hành bấm nút giao hàng
        for(OrderDetail detail : order.getOrderDetails()){
            Product p = pRepo.getById(detail.getProductId());
            if(p.getStock() < 0){ //nếu Stock bị trừ lúc tạo thì đảm bảo sau khi tạo không âm
                throw new RuntimeException("Sản phẩm " + p.getName() + " hiện đã hết hàng");
            }
        }

        order.setStatus("Đã xác nhận");
        oRepo.update(order);
        return order;
    }

    public PageResponse<Order> getAllOrdersAdmin(int page, String searchTerm, String status) {
        int pageSize = 10;

        List<Order> orders = oRepo.getAllAdmin(page, pageSize, searchTerm, status);

        int totalItems = oRepo.countAllAdmin(searchTerm, status);

        return new PageResponse<>(orders, totalItems, page, pageSize);
    }
}
