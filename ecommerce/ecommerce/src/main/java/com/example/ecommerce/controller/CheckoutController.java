package com.example.ecommerce.controller;

import com.example.ecommerce.entity.GroupBuy;
import com.example.ecommerce.service.GroupBuyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class CheckoutController {

    private final GroupBuyService groupBuyService;

    public CheckoutController(GroupBuyService groupBuyService) {
        this.groupBuyService = groupBuyService;
    }

    @GetMapping("/checkout/{groupId}")
    public String showCheckout(@PathVariable Long groupId, Model model, RedirectAttributes redirect) {
        GroupBuy groupBuy = groupBuyService.getGroupBuyById(groupId);
        if (groupBuy == null || !groupBuy.isFull()) {
            redirect.addFlashAttribute("error", "গ্রুপ পূর্ণ নেই বা পাওয়া যায়নি");
            return "redirect:/group-buy/" + groupId;
        }

        // Discounted price calculate
        BigDecimal originalPrice = groupBuy.getProduct().getPrice();
        BigDecimal discountRate = BigDecimal.valueOf(groupBuy.getDiscountPercentage() / 100);
        BigDecimal discountedPrice = originalPrice.subtract(originalPrice.multiply(discountRate));

        model.addAttribute("groupBuy", groupBuy);
        model.addAttribute("discountedPrice", discountedPrice);

        return "checkout"; // templates/checkout.html
    }

    @PostMapping("/checkout/{groupId}")
    public String completeOrder(@PathVariable Long groupId, RedirectAttributes redirect) {
        GroupBuy groupBuy = groupBuyService.getGroupBuyById(groupId);
        if (groupBuy == null || !groupBuy.isFull()) {
            redirect.addFlashAttribute("error", "অর্ডার কমপ্লিট করতে পারল না");
            return "redirect:/checkout/" + groupId;
        }

        // Group status update (order complete)
        groupBuy.setStatus(GroupBuy.GroupStatus.COMPLETED);
        groupBuyService.save(groupBuy);

        redirect.addFlashAttribute("success", "অর্ডার কমপ্লিট! ধন্যবাদ।");
        return "redirect:/orders"; // অথবা /my-orders
    }
}