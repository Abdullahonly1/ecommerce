package com.example.ecommerce.repository;

import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CartItemRepository – Spring Data JPA দিয়ে অটো CRUD অপারেশন পাবে
 * Long = CartItem entity-এর @Id এর টাইপ
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // অটো পাবে:
    // - findAll()
    // - findById(Long id)
    // - save(CartItem item)
    // - deleteById(Long id)
    // - deleteAll() ইত্যাদি

    // নতুন যোগ করা হয়েছে – ইউজার-স্পেসিফিক কোয়েরি (ধাপ ২-এর জন্য)
    List<CartItem> findByUser(User user);  // ঐ ইউজারের সব কার্ট আইটেম খোঁজা

    Optional<CartItem> findByUserAndProduct(User user, Product product);  // ঐ ইউজারের ঐ প্রোডাক্ট আছে কি না চেক

    Optional<CartItem> findByIdAndUser(Long id, User user);  // ঐ ইউজারের নির্দিষ্ট আইটেম খোঁজা (update/remove-এর জন্য)

    void deleteByUser(User user);  // ঐ ইউজারের সব কার্ট আইটেম মুছে ফেলা (clear cart-এর জন্য)

    // পরে চাইলে আরও কাস্টম কোয়েরি যোগ করতে পারো, উদাহরণ:
    // List<CartItem> findByUserAndQuantityGreaterThan(User user, int minQuantity);
}