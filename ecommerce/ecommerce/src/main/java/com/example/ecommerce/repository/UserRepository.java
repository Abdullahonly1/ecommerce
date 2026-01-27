package com.example.ecommerce.repository;

import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ইতিমধ্যে থাকলে ভালো, না থাকলে যোগ করো
    Optional<User> findByUsername(String username);

    // এই লাইনটা যোগ করো (email দিয়ে খুঁজবে)
    Optional<User> findByEmail(String email);
}