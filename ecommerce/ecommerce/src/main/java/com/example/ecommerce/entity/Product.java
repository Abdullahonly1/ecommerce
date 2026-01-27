package com.example.ecommerce.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/*
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;
    private int stock;
    private String imageUrl;
    private String description;

    public Product() {}

    public Product(String name, double price, int stock, String imageUrl, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
*/

/*
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    //private double price;
    private int stock;
    @Column(name = "image_url")   // 🔴 IMPORTANT
    private String imageUrl;
    private String description;
    private String category;  // <--- এটা নতুন যোগ করো (e.g., "Women's Fashion")
    // Product.java এ
    @Column(name = "price")
    private BigDecimal price;  // double এর বদলে BigDecimal

    public BigDecimal getPrice() {
        return price;
    }

    // ডিফল্ট constructor (Hibernate-এর জন্য জরুরি)
    public Product() {}



    // 5 parameter constructor (initData() এর জন্য)


    public Product(String name, String description, double price, int stock, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public boolean isInStock() {
        return stock > 0;
    }
}*/



@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)  // ডাটাবেসে সঠিক টাইপ
    private BigDecimal price;  // double এর বদলে BigDecimal (সবচেয়ে ভালো)

    private int stock;

    @Column(name = "image_url")
    private String imageUrl;

    private String description;

    private String category;  // ক্যাটাগরি ফিল্ড রাখা আছে

    // ডিফল্ট কনস্ট্রাক্টর (Hibernate-এর জন্য জরুরি)
    public Product() {}

    // তোমার পুরোনো ৬ প্যারামিটার কনস্ট্রাক্টর (যেটা initData() এর জন্য ব্যবহার হয়)
    public Product(String name, String description, double price, int stock, String imageUrl, String category) {
        this.name = name;
        this.description = description;
        this.price = BigDecimal.valueOf(price);  // double কে BigDecimal-এ কনভার্ট
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    // নতুন কনস্ট্রাক্টর (যদি চাও – সব ডাটা সহ)
    public Product(String name, BigDecimal price, int stock, String imageUrl, String description, String category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.description = description;
        this.category = category;
    }

    // Getters and Setters (সব রাখা আছে + ডুপ্লিকেট মুছে দেওয়া হয়েছে)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // স্টক চেক (যদি দরকার হয়)
    public boolean isInStock() {
        return stock > 0;
    }
}