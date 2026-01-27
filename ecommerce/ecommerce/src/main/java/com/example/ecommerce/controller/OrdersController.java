package com.example.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrdersController {

    @GetMapping("/orders")
    public String showOrders(Model model) {
        // তোমার order list load logic (যদি থাকে)
        // model.addAttribute("orders", orderService.getUserOrders());

        return "order-success"; // templates/order-success.html পেজে যাবে
    }
}