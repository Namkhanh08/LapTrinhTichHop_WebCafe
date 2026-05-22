package com.example.Java_Backend.repositories;

import com.example.Java_Backend.entities.Cart;
import com.example.Java_Backend.entities.CartItem;
import com.example.Java_Backend.entities.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public class CartRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<CartItem> getCartItemsByUser(String userId){

        String sql = """
                    SELECT
                        CI.Id,
                        CI.CartId,
                        CI.ProductId,
                        CI.Quantity,
                        CI.GrindingOptionId,
                        CI.FlavorNotes,
                        CI.Weight,
                        P.Price, P.Name, P.ImageUrl
                    FROM CartItems CI
                    INNER JOIN Carts C ON C.Id = CI.CartId\s
                    INNER JOIN Products P ON P.Id = CI.ProductId
                    WHERE C.UserId = ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CartItem item = new CartItem();

            item.setId(rs.getInt("Id"));
            item.setCartId(rs.getInt("CartId"));
            item.setProductId(rs.getInt("ProductId"));
            item.setQuantity(rs.getInt("Quantity"));
            item.setGrindingOptionId(rs.getInt("GrindingOptionId"));
            item.setFlavorNotes(rs.getString("FlavorNotes"));
            item.setWeight(rs.getString("Weight"));

            Product p = new Product();

            p.setName(rs.getString("Name"));
            p.setImageUrl(rs.getString("ImageUrl"));
            p.setPrice(rs.getDouble("Price"));
            item.setProduct(p);


            return item;
        }, userId);
    }

    public Cart findByUserId(String userId){
        String sql = "SELECT * FROM Carts WHERE UserId = ?";

        return jdbcTemplate.query(sql, rs -> {
            if(rs.next()){
                Cart c = new Cart();
                c.setId(rs.getInt("Id"));
                c.setUserId(rs.getString("UserId"));
                return c;
            }
            return null;
        }, userId);
    }

    public void createCart(String userId){
        String sql = "INSERT INTO Carts(UserId) VALUES (?)";
        jdbcTemplate.update(sql, userId);
    }

    public CartItem findByCartAndProduct(int cartId, int productId, int grindingOptionId, String flavorNotes, String weight)
    {
        String normalizedFlavorNotes = (flavorNotes == null || flavorNotes.trim().isEmpty()) ? null : flavorNotes.trim();
        String sql = """
        SELECT *
        FROM CartItems
        WHERE CartId = ?
          AND ProductId = ?
          AND GrindingOptionId = ?
          AND (
                (? IS NULL AND FlavorNotes IS NULL)
                OR FlavorNotes = ?
              )
          AND Weight = ?
    """;

        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                CartItem ci = new CartItem();
                ci.setId(rs.getInt("Id"));
                ci.setCartId(rs.getInt("CartId"));
                ci.setProductId(rs.getInt("ProductId"));
                ci.setQuantity(rs.getInt("Quantity"));
                ci.setGrindingOptionId(rs.getInt("GrindingOptionId"));
                ci.setFlavorNotes(rs.getString("FlavorNotes"));
                ci.setWeight(rs.getString("Weight"));
                return ci;
            }
            return null;
        }, cartId, productId, grindingOptionId, normalizedFlavorNotes, normalizedFlavorNotes, weight);
    }

    public void addItem(int cartId, int productId, int quantity, int grindingOptionId, String flavorNotes, String weight){
        String sql = "INSERT INTO CartItems(CartId, ProductId, Quantity, GrindingOptionId, FlavorNotes, Weight) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, cartId, productId, quantity, grindingOptionId, flavorNotes, weight);
    }

    public void updateQuantity(String userId, int productId, int newQuantity, int grindingOptionId, String flavorNotes, String weight){
        String sql = "UPDATE CI SET CI.Quantity = ? FROM CartItems CI INNER JOIN Carts C ON C.Id = CI.CartId WHERE CI.ProductId = ? AND CI.GrindingOptionId = ? AND CI.FlavorNotes = ? AND C.UserId = ? AND CI.Weight = ?";
        jdbcTemplate.update(sql, newQuantity, productId, grindingOptionId, flavorNotes, userId, weight);
    }

    public void deleteItem(int productId, int grindingOptionId, String flavorNotes, String weight){
        String sql = "DELETE FROM CartItems WHERE ProductId = ? AND GrindingOptionId = ? AND FlavorNotes = ? AND Weight = ?";
        jdbcTemplate.update(sql, productId, grindingOptionId, flavorNotes, weight);
    }
}
