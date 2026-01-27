package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ক্যাটাগরি অনুসারে সব প্রোডাক্ট খোঁজা
    List<Product> findByCategory(String category);

    // নাম বা ডেসক্রিপশন অনুসারে সার্চ (LIKE %query%)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', ?1, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Product> searchProducts(String query);

    // নাম দিয়ে ঠিক প্রোডাক্ট খোঁজা (case-insensitive, Optional রিটার্ন)
    Optional<Product> findByNameIgnoreCase(String name);

    // অতিরিক্ত হেল্পফুল মেথড (যদি চাও, পরে ব্যবহার করতে পারো)
    // List<Product> findByNameContainingIgnoreCase(String namePart);
    // Optional<Product> findByIdAndStockGreaterThan(Long id, Integer minStock);
}