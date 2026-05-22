package com.example.order_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    // --- Bổ sung các thuộc tính của hạt cà phê từ BC2.sql ---
    @Column(name = "flavor_notes")
    private String flavorNotes;

    @Column(name = "grinding_option_id")
    private Integer grindingOptionId;

    @Column(name = "weight")
    private String weight;

    @PrePersist
    @PreUpdate
    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getFlavorNotes() { return flavorNotes; }
    public void setFlavorNotes(String flavorNotes) { this.flavorNotes = flavorNotes; }

    public Integer getGrindingOptionId() { return grindingOptionId; }
    public void setGrindingOptionId(Integer grindingOptionId) { this.grindingOptionId = grindingOptionId; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    // --- custom methods & PascalCase support ---
    public static class ProductDto {
        private String name;
        private String imageUrl;

        public ProductDto(String name, String imageUrl) {
            this.name = name;
            this.imageUrl = imageUrl;
        }

        @JsonProperty("Name")
        public String getName() { return name; }
        @JsonProperty("ImageUrl")
        public String getImageUrl() { return imageUrl; }
    }

    @JsonIgnore
    public ProductDto getProduct() {
        String img = "/assets/img/section2/image1.png";
        if (productId != null) {
            if (productId.contains("2")) img = "/assets/img/section2/image2.png";
            else if (productId.contains("3")) img = "/assets/img/section2/image3.png";
            else if (productId.contains("4")) img = "/assets/img/section2/image4.png";
            else if (productId.contains("5")) img = "/assets/img/section2/image5.png";
        }
        return new ProductDto(this.productName, img);
    }

    @JsonProperty("Product")
    public ProductDto getPascalProduct() {
        return getProduct();
    }

    @JsonProperty("ProductId")
    public String getPascalProductId() { return productId; }

    @JsonProperty("ProductName")
    public String getPascalProductName() { return productName; }

    @JsonProperty("Quantity")
    public Integer getPascalQuantity() { return quantity; }

    @JsonProperty("UnitPrice")
    public BigDecimal getPascalUnitPrice() { return unitPrice; }

    @JsonProperty("Subtotal")
    public BigDecimal getPascalSubtotal() { return subtotal; }

    @JsonProperty("FlavorNotes")
    public String getPascalFlavorNotes() { return flavorNotes; }

    @JsonProperty("GrindingOptionId")
    public Integer getPascalGrindingOptionId() { return grindingOptionId; }

    @JsonProperty("Weight")
    public String getPascalWeight() { return weight; }
}
