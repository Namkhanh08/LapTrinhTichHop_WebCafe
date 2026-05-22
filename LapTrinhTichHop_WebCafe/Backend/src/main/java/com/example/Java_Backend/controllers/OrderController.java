package com.example.Java_Backend.controllers;

import com.example.Java_Backend.Dtos.CreateOrderDto;
import com.example.Java_Backend.Dtos.PageResponse;
import com.example.Java_Backend.Dtos.UpdateStatusDto;
import com.example.Java_Backend.Dtos.UpdateOrderDto;
import com.example.Java_Backend.entities.Order;
import com.example.Java_Backend.services.JwtService;
import com.example.Java_Backend.services.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin("*")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtService jwtService;

    private String getUserIdFromHeader(String authHeader){
        String token = authHeader.replace("Bearer ", "");
        return jwtService.extractUserId(token);
    }

    @GetMapping
    public List<Order> getMyOrders(@RequestHeader("Authorization") String authHeader){
        String userId = getUserIdFromHeader(authHeader);
        return orderService.getMyOrders(userId);
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable int id){
        return orderService.getById(id);
    }

    @GetMapping("/all")
    public List<Order> getAll(){
        return orderService.getAllOrders();
    }

    @PostMapping
    public Order createOrder(@RequestHeader("Authorization") String authHeader, @RequestBody CreateOrderDto dto){
        String userId = getUserIdFromHeader(authHeader);
        return orderService.createOrder(userId, dto);
    }

    @PutMapping("/{id}")
    public Order updateOrder(@PathVariable int id, @RequestBody UpdateOrderDto dto){
        return orderService.updateOrder(id, dto);
    }

    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable int id, @RequestParam String status){
        return orderService.updateStatus(id, status);
    }

    @PutMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable int id){
        orderService.cancelOrder(id);
        return "Hủy đơn hàng thành công!";
    }


    //ADMIN
    @GetMapping("/admin/all")
    public ResponseEntity<PageResponse<Order>> getAllAdmin(@RequestParam(defaultValue = "1") int page, @RequestParam(required = false) String searchTerm, @RequestParam(defaultValue = "all") String status){
        PageResponse<Order> response = orderService.getAllOrdersAdmin(page, searchTerm, status);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable int id){
        try{
            Order confirmedOrder = orderService.confirmOrder(id);
            return ResponseEntity.ok(confirmedOrder);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
