package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class ChatController {

    @Autowired
    private ProductRepository productRepository;

    @MessageMapping("/chat")
    @SendTo("/topic/chat")
    public String handleChatMessage(String message) {
        System.out.println("=== CHAT CONTROLLER CALLED ===");
        System.out.println("User sent: " + message);

        String lowerMessage = message.toLowerCase().trim();
        String productName = extractProductName(lowerMessage);

        if (productName != null && !productName.isEmpty()) {
            System.out.println("Searching product: " + productName);
            Optional<Product> productOpt = productRepository.findByNameIgnoreCase(productName);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                System.out.println("Product found: " + product.getName() + " | Stock: " + product.getStock());
                if (product.getStock() > 0) {
                    return "হ্যাঁ, " + product.getName() + " স্টকে আছে! পরিমাণ: " + product.getStock() + " পিস।";
                } else {
                    return "দুঃখিত, " + product.getName() + " স্টকে নেই।";
                }
            } else {
                System.out.println("Product not found: " + productName);
                return "দুঃখিত, '" + productName + "' পাওয়া যায়নি।";
            }
        }

        System.out.println("No product name found in message");
        return "হাই! প্রোডাক্টের স্টক জানতে চাইলে বলুন, যেমন: 'Is Summer Dress in stock?'";
    }

    private String extractProductName(String message) {
        message = message.replace("?", "").replace("!", "").trim();

        String[] patterns = {
                "is (.*) in stock",
                "(.*) in stock",
                "(.*) stock e ache",
                "(.*) available",
                "(.*) আছে কি",
                "stock (.*)"
        };

        for (String pattern : patterns) {
            if (message.matches(".*" + pattern + ".*")) {
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(message);
                if (matcher.find()) {
                    String name = matcher.group(1).trim();
                    return cleanProductName(name);
                }
            }
        }

        return cleanProductName(message);
    }

    private String cleanProductName(String name) {
        name = name.replaceAll("(?i)(is|in|stock|e|ache|ki|available|আছে|কি)", "").trim();
        name = name.replaceAll("\\s+", " ");
        return name.isEmpty() ? null : name;
    }
}