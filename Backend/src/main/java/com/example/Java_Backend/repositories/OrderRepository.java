package com.example.Java_Backend.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.Java_Backend.entities.Product;
import com.example.Java_Backend.entities.OrderDetail;
import com.example.Java_Backend.entities.Order;
import com.example.Java_Backend.Dtos.TopProductDto;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.time.LocalDateTime;

@Repository
public class OrderRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Order> GetByUserAsync(String userId){
        String sql = "SELECT * FROM Orders WHERE UserId = ? ORDER BY OrderDate DESC";

        List<Order> orders =  jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order o = new Order();

            o.setId(rs.getInt("Id"));
            o.setUserId(rs.getString("UserId"));
            o.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
            o.setTotalAmount(rs.getDouble("TotalAmount"));
            o.setStatus(rs.getString("Status"));
            o.setShippingDetailAddress(rs.getString("ShippingDetailAddress"));
            o.setPaymentMethod(rs.getString("PaymentMethod"));
            o.setReceiverName(rs.getString("ReceiverName"));
            o.setReceiverPhone(rs.getString("ReceiverPhone"));
            o.setShippingWard(rs.getString("ShippingWard"));
            o.setShippingDistrict(rs.getString("ShippingDistrict"));
            o.setShippingProvince(rs.getString("ShippingProvince"));
            o.setShippingNote(rs.getString("ShippingNote"));
            o.setDiscountAmount(rs.getDouble("DiscountAmount"));
            o.setFinalAmount(rs.getDouble("FinalAmount"));
            o.setVoucherCode(rs.getString("VoucherCode"));

            return o;
        },userId);
        for(Order order : orders){
            order.setOrderDetails(getByOrder(order.getId()));
        }

        return orders;
    }

    public Order getById(int orderId){
        String sql = "SELECT * FROM Orders WHERE Id = ?";

        List<Order> orders = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order o = new Order();

            o.setId(rs.getInt("Id"));
            o.setUserId(rs.getString("UserId"));
            o.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
            o.setTotalAmount(rs.getDouble("TotalAmount"));
            o.setStatus(rs.getString("Status"));
            o.setShippingDetailAddress(rs.getString("ShippingDetailAddress"));
            o.setReceiverName(rs.getString("ReceiverName"));
            o.setReceiverPhone(rs.getString("ReceiverPhone"));
            o.setShippingProvince(rs.getString("ShippingProvince"));
            o.setShippingDistrict(rs.getString("ShippingDistrict"));
            o.setShippingWard(rs.getString("ShippingWard"));
            o.setShippingNote(rs.getString("ShippingNote"));
            o.setPaymentMethod(rs.getString("PaymentMethod"));
            o.setDiscountAmount(rs.getDouble("DiscountAmount"));
            o.setFinalAmount(rs.getDouble("FinalAmount"));
            o.setVoucherCode(rs.getString("VoucherCode"));

            return o;
        }, orderId);

        if(orders.isEmpty()) return null;

        Order order = orders.get(0);
        order.setOrderDetails(getByOrder(orderId));

        return order;
    }

    public List<Order> getAll() {
        String sql = "SELECT * FROM Orders ORDER BY OrderDate DESC";

        List<Order> orders = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order o = new Order();
            o.setId(rs.getInt("Id"));
            o.setUserId(rs.getString("UserId"));
            o.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
            o.setTotalAmount(rs.getDouble("TotalAmount"));
            o.setStatus(rs.getString("Status"));
            o.setShippingDetailAddress(rs.getString("ShippingDetailAddress"));
            o.setPaymentMethod(rs.getString("PaymentMethod"));

            o.setReceiverName(rs.getString("ReceiverName"));
            o.setReceiverPhone(rs.getString("ReceiverPhone"));
            o.setShippingProvince(rs.getString("ShippingProvince"));
            o.setShippingDistrict(rs.getString("ShippingDistrict"));
            o.setShippingWard(rs.getString("ShippingWard"));
            o.setShippingNote(rs.getString("ShippingNote"));
            o.setDiscountAmount(rs.getDouble("DiscountAmount"));
            o.setFinalAmount(rs.getDouble("FinalAmount"));
            o.setVoucherCode(rs.getString("VoucherCode"));

            return o;
        });

        for (Order order : orders) {
            order.setOrderDetails(getByOrder(order.getId()));
        }

        return orders;
    }

    public List<OrderDetail> getByOrder(int orderId){
        String sql = """
            SELECT
                OD.Id,
                OD.OrderId,
                OD.ProductId,
                OD.Quantity,
                OD.UnitPrice,
                OD.FlavorNotes,
                OD.GrindingOptionId,
                OD.Weight,

                P.Name,
                P.Price,
                P.ImageUrl

            FROM OrderDetails OD
            INNER JOIN Products P ON P.Id = OD.ProductId
            WHERE OD.OrderId = ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {

            OrderDetail od = new OrderDetail();

            od.setId(rs.getInt("Id"));
            od.setOrderId(rs.getInt("OrderId"));
            od.setProductId(rs.getInt("ProductId"));
            od.setQuantity(rs.getInt("Quantity"));
            od.setUnitPrice(rs.getDouble("UnitPrice"));
            od.setFlavorNotes(rs.getString("FlavorNotes"));
            od.setGrindingOptionId(rs.getInt("GrindingOptionId"));
            od.setWeight(rs.getString("Weight"));

            Product p = new Product();

            p.setName(rs.getString("Name"));
            p.setPrice(rs.getDouble("Price"));
            p.setImageUrl(rs.getString("ImageUrl"));
            od.setProduct(p);

            return od;

            }, orderId);
    }

    public void create (Order order){
        String sql = "INSERT INTO Orders (UserId, OrderDate, TotalAmount, Status, ReceiverName, ReceiverPhone, ShippingProvince, ShippingDistrict, ShippingWard, ShippingDetailAddress, ShippingNote, PaymentMethod, VoucherCode, DiscountAmount, FinalAmount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, order.getUserId());
            ps.setObject(2, order.getOrderDate());
            ps.setDouble(3, order.getTotalAmount());
            ps.setString(4, order.getStatus());
            ps.setString(5, order.getReceiverName());
            ps.setString(6, order.getReceiverPhone());
            ps.setString(7, order.getShippingProvince());
            ps.setString(8, order.getShippingDistrict());
            ps.setString(9, order.getShippingWard());
            ps.setString(10, order.getShippingDetailAddress());
            ps.setString(11, order.getShippingNote());
            ps.setString(12, order.getPaymentMethod());
            ps.setString(13, order.getVoucherCode());
            ps.setDouble(14,order.getDiscountAmount());
            ps.setDouble(15, order.getFinalAmount());

            return ps;
        }, keyHolder);

        int orderId = keyHolder.getKey().intValue();
        order.setId(orderId);

        for(OrderDetail detail : order.getOrderDetails()){
            String detailSql = "INSERT INTO OrderDetails (OrderId, ProductId, Quantity, UnitPrice, FlavorNotes, GrindingOptionId, Weight) VALUES (?,?,?,?,?,?,?)";
            jdbcTemplate.update(detailSql, orderId, detail.getProductId(), detail.getQuantity(), detail.getUnitPrice(), detail.getFlavorNotes(), detail.getGrindingOptionId(), detail.getWeight());
        }
    }
    public void update(Order order){
        String sql = "UPDATE Orders SET Status = ?, TotalAmount = ?, ShippingDetailAddress = ? WHERE Id = ?";
        jdbcTemplate.update(sql, order.getStatus(), order.getTotalAmount(), order.getShippingDetailAddress(), order.getId());
    }

    public void updateFullOrder(Order order){

        String sql = """
        UPDATE Orders
        SET
            ReceiverName = ?,
            ReceiverPhone = ?,
            ShippingProvince = ?,
            ShippingDistrict = ?,
            ShippingWard = ?,
            ShippingDetailAddress = ?,
            ShippingNote = ?,
            PaymentMethod = ?,
            Status = ?
        WHERE Id = ?
    """;

        jdbcTemplate.update(
                sql,
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getShippingProvince(),
                order.getShippingDistrict(),
                order.getShippingWard(),
                order.getShippingDetailAddress(),
                order.getShippingNote(),
                order.getPaymentMethod(),
                order.getStatus(),
                order.getId()

        );
    }



    // ADMIN
    public double getExpectedRevenue(){
        String sql = "SELECT ISNULL(SUM(TotalAmount),0) FROM Orders WHERE Status NOT IN ('Chờ thanh toán', 'Đã hủy')";
        return jdbcTemplate.queryForObject(sql, Double.class);
    }

    public int getPendingOrdersCount(){
        String sql = "SELECT COUNT(*) FROM Orders WHERE Status != 'Đã hủy'";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Order> getLastestOrders(){
        String sql = "SELECT TOP 5 * FROM Orders ORDER BY OrderDate DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order o = new Order();

            o.setId(rs.getInt("Id"));
            o.setReceiverName(rs.getString("ReceiverName"));
            o.setTotalAmount(rs.getDouble("TotalAmount"));
            o.setStatus(rs.getString("Status"));
            o.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
            return o;
        });
    }

    public List<TopProductDto> getTopProducts(){
        String sql = "SELECT TOP 5 P.Id, P.Name, P.ImageUrl, SUM(OD.Quantity) AS TotalSold " +
                "FROM OrderDetails OD " +
                "INNER JOIN Products P ON P.Id = OD.ProductId " +
                "INNER JOIN Orders O ON O.Id = OD.OrderId " +
                "WHERE O.Status != 'Đã hủy' " +
                "GROUP BY P.Id, P.Name, P.ImageUrl " +
                "ORDER BY TotalSold DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TopProductDto dto = new TopProductDto();

            dto.setProductId(rs.getInt("Id"));
            dto.setProductName(rs.getString("Name"));
            dto.setImageUrl(rs.getString("ImageUrl"));
            dto.setTotalSold(rs.getInt("TotalSold"));

            return dto;
        });
    }

    public List<Order> getAllAdmin(int page, int pageSize, String searchTerm, String status){

        StringBuilder sql = new StringBuilder("SELECT * FROM Orders WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if(status != null && !status.equalsIgnoreCase("all")){
            sql.append(" AND Status = ?");
            params.add(status);
        }

        if(searchTerm != null && !searchTerm.isEmpty()){
            sql.append(" AND (CAST(Id AS NVARCHAR) LIKE ? OR ReceiverName LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }

        sql.append(" ORDER BY OrderDate DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<Order> orders = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {

            Order o = new Order();

            o.setId(rs.getInt("Id"));
            o.setUserId(rs.getString("UserId"));
            o.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
            o.setTotalAmount(rs.getDouble("TotalAmount"));
            o.setStatus(rs.getString("Status"));

            o.setReceiverName(rs.getString("ReceiverName"));
            o.setReceiverPhone(rs.getString("ReceiverPhone"));
            o.setShippingProvince(rs.getString("ShippingProvince"));
            o.setShippingDistrict(rs.getString("ShippingDistrict"));
            o.setShippingWard(rs.getString("ShippingWard"));
            o.setShippingNote(rs.getString("ShippingNote"));
            o.setPaymentMethod(rs.getString("PaymentMethod"));
            o.setDiscountAmount(rs.getDouble("DiscountAmount"));
            o.setFinalAmount(rs.getDouble("FinalAmount"));
            o.setVoucherCode(rs.getString("VoucherCode"));

            return o;

        }, params.toArray());

        // QUAN TRỌNG
        for(Order order : orders){
            order.setOrderDetails(getByOrder(order.getId()));
        }

        return orders;
    }

    public int countAllAdmin(String searchTerm, String status){
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Orders WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if(status != null && !status.equalsIgnoreCase("all")){
            sql.append(" AND Status = ?");
            params.add(status);
        }
        if(searchTerm != null && !searchTerm.isEmpty()){
            sql.append(" AND (Id LIKE ? OR ReceiverName LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }


    //SHIPPER
    public List<Order> getOrdersForShipper(int page, int pageSize, String searchTerm) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Orders WHERE Status = N'Đang trung chuyển' \n" +
                "OR Status = N'Shipper đã nhận' \n" +
                "OR Status = N'Đang giao'");
        List<Object> params = new ArrayList<>();

        // Nếu shipper muốn tìm kiếm nhanh mã đơn hoặc tên khách trong tập đơn đang giao
        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append(" AND (CAST(Id AS NVARCHAR) LIKE ? OR ReceiverName LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }

        sql.append(" ORDER BY OrderDate DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * pageSize);
        params.add(pageSize);

        List<Order> orders = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Order o = new Order();
            o.setId(rs.getInt("Id"));
            o.setUserId(rs.getString("UserId"));
            o.setOrderDate(rs.getTimestamp("OrderDate").toLocalDateTime());
            o.setTotalAmount(rs.getDouble("TotalAmount"));
            o.setStatus(rs.getString("Status"));
            o.setReceiverName(rs.getString("ReceiverName"));
            o.setReceiverPhone(rs.getString("ReceiverPhone"));
            o.setShippingProvince(rs.getString("ShippingProvince"));
            o.setShippingDistrict(rs.getString("ShippingDistrict"));
            o.setShippingWard(rs.getString("ShippingWard"));
            o.setShippingDetailAddress(rs.getString("ShippingDetailAddress"));
            o.setShippingNote(rs.getString("ShippingNote"));
            o.setPaymentMethod(rs.getString("PaymentMethod"));
            o.setDiscountAmount(rs.getDouble("DiscountAmount"));
            o.setFinalAmount(rs.getDouble("FinalAmount"));
            o.setVoucherCode(rs.getString("VoucherCode"));
            return o;
        }, params.toArray());

        // Đổ chi tiết sản phẩm để shipper kiểm đếm hàng hóa
        for (Order order : orders) {
            order.setOrderDetails(getByOrder(order.getId()));
        }

        return orders;
    }

    public int countOrdersForShipper(String searchTerm) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Orders WHERE Status = N'Đang giao' OR Status = N'Shipper đã nhận' OR Status = N'Đang trung chuyển'" );
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append(" AND (CAST(Id AS NVARCHAR) LIKE ? OR ReceiverName LIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }
}
