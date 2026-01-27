package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * CartItem entity – কার্টের আইটেম।
 * User যোগ করা হয়েছে যাতে কার্ট ইউজার-স্পেসিফিক হয়।
 */
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)  // EAGER লোডিং যোগ করা হয়েছে (product সবসময় লোড হবে)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)  // ← নতুন যোগ: User যোগ করা হয়েছে (কার্ট ইউজার-স্পেসিফিক হবে)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    // ডিফল্ট কনস্ট্রাক্টর (Hibernate-এর জন্য জরুরি)
    public CartItem() {}

    // প্রধান কনস্ট্রাক্টর – Product + User দিয়ে CartItem বানানোর জন্য
    public CartItem(Product product, User user) {
        if (product == null || user == null) {
            throw new IllegalArgumentException("Product and User cannot be null");
        }
        this.product = product;
        this.user = user;
        this.price = product.getPrice();  // product থেকে price নেওয়া
        this.quantity = 1;  // ডিফল্ট কোয়ান্টিটি ১
    }

    // তোমার পুরোনো ফুল কনস্ট্রাক্টর (যদি কোথাও ব্যবহার হয়, রাখা আছে)
    public CartItem(Long id, Product product, Integer quantity, BigDecimal price) {
        this.id = id;
        this.product = product;
        this.quantity = quantity != null ? quantity : 1;
        this.price = price;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getUser() { return user; }  // ← নতুন যোগ
    public void setUser(User user) { this.user = user; }  // ← নতুন যোগ

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) {
        this.quantity = (quantity != null && quantity > 0) ? quantity : 1;
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    // সাব-টোটাল (null-safe + ক্র্যাশ প্রুফ)
    public BigDecimal getTotalPrice() {
        if (price == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    // Cart.html-এর জন্য সহজ getter (product থেকে নেওয়া)
    public String getImageUrl() {
        return product != null ? product.getImageUrl() : null;
    }

    public String getProductName() {
        return product != null ? product.getName() : "Unknown Product";
    }
}