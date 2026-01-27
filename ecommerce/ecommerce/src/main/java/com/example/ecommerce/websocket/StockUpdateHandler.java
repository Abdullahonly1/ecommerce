package com.example.ecommerce.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Stock আপডেট করার সময় সব ক্লায়েন্টকে রিয়েল-টাইম নোটিফিকেশন পাঠানোর হ্যান্ডলার।
 * এটা WebSocket দিয়ে কাজ করে (রিফ্রেশ ছাড়া স্টক আপডেট হয়)।
 */
@Component
public class StockUpdateHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public StockUpdateHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * নির্দিষ্ট প্রোডাক্টের নতুন স্টক সব ক্লায়েন্টকে পাঠায়।
     * ক্লায়েন্টরা /topic/stock/{productId} সাবস্ক্রাইব করে আপডেট পাবে।
     *
     * @param productId প্রোডাক্টের ID
     * @param newStock নতুন স্টক পরিমাণ
     */
    public void broadcastStockUpdate(Long productId, int newStock) {
        if (productId == null) {
            System.out.println("Warning: broadcastStockUpdate called with null productId");
            return;
        }

        String destination = "/topic/stock/" + productId;
        System.out.println("Broadcasting stock update for product " + productId + ": " + newStock + " to " + destination);

        try {
            messagingTemplate.convertAndSend(destination, newStock);
        } catch (Exception e) {
            System.err.println("Failed to broadcast stock update for product " + productId + ": " + e.getMessage());
        }
    }
}