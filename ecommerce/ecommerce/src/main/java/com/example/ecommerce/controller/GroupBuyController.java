package com.example.ecommerce.controller;

import com.example.ecommerce.entity.GroupBuy;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.GroupBuyService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/group-buy")
public class GroupBuyController {

    private final GroupBuyService groupBuyService;
    private final ProductService productService;
    private final UserService userService;
    private final UserRepository userRepository;

    @Autowired
    public GroupBuyController(GroupBuyService groupBuyService,
                              ProductService productService,
                              UserService userService,
                              UserRepository userRepository) {
        this.groupBuyService = groupBuyService;
        this.productService = productService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // গ্রুপ বাই পেজ / গ্রুপ তৈরি + ডিটেইল
    @GetMapping("/{productId}")
    public String handleGroupBuy(@PathVariable Long productId,
                                 Principal principal,
                                 Model model,
                                 RedirectAttributes redirect,
                                 HttpSession session) {

        System.out.println("=== handleGroupBuy STARTED === productId: " + productId);

        if (principal == null || principal.getName() == null) {
            System.out.println("No authenticated user → redirecting to login");
            redirect.addFlashAttribute("nextUrl", "/group-buy/" + productId);
            return "redirect:/login";
        }

        String username = principal.getName();
        System.out.println("Authenticated user: " + username);

        User creator = userRepository.findByUsername(username).orElse(null);
        if (creator == null) {
            System.out.println("User not found in DB: " + username);
            redirect.addFlashAttribute("error", "ইউজার পাওয়া যায়নি");
            return "redirect:/";
        }
        System.out.println("Creator found - ID: " + creator.getId());

        Product product = productService.findById(productId).orElse(null);
        if (product == null) {
            System.out.println("Product not found: " + productId);
            redirect.addFlashAttribute("error", "প্রোডাক্ট পাওয়া যায়নি");
            return "redirect:/";
        }
        System.out.println("Product found - ID: " + product.getId() + ", Name: " + product.getName());

        try {
            // Session cache clear
            System.out.println("Clearing session attribute 'groupBuy'");
            session.removeAttribute("groupBuy");

            System.out.println("Checking for existing group...");
            Optional<GroupBuy> existingGroup = groupBuyService.getGroupBuyByProductAndCreator(productId, creator.getId());

            GroupBuy groupBuy;

            if (existingGroup.isPresent()) {
                groupBuy = existingGroup.get();
                System.out.println("Existing group FOUND - Group ID: " + groupBuy.getId());
            } else {
                System.out.println("No existing group → creating NEW group");
                groupBuy = groupBuyService.createGroupBuy(product, creator);
                System.out.println("New group CREATED - Group ID: " + groupBuy.getId());
            }

            // Null-safety চেক
            if (groupBuy.getGroupLink() == null) {
                System.out.println("CRITICAL: groupLink is NULL after creation/fetch");
                redirect.addFlashAttribute("error", "গ্রুপ লিঙ্ক জেনারেট হয়নি");
                return "redirect:/product/" + productId;
            }

            String shareUrl = "http://localhost:8080/group-buy/join/" + groupBuy.getGroupLink();
            boolean isCreator = groupBuy.getCreator() != null && groupBuy.getCreator().getId().equals(creator.getId());

            System.out.println("Preparing model attributes...");
            model.addAttribute("groupBuy", groupBuy);
            model.addAttribute("product", product);
            model.addAttribute("shareUrl", shareUrl);
            model.addAttribute("isCreator", isCreator);

            System.out.println("=== handleGroupBuy SUCCESS === Returning group-buy template");
            System.out.println("Group ID: " + groupBuy.getId() + " | Share URL: " + shareUrl);
            System.out.println("=== END OF handleGroupBuy ===");

            return "group-buy";

        } catch (Exception e) {
            System.out.println("=== handleGroupBuy CRASHED ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "গ্রুপ লোড/তৈরিতে সমস্যা হয়েছে: " + e.getMessage());
            return "redirect:/product/" + productId;
        }
    }

    // জয়েন করা (লিঙ্ক থেকে)
    @GetMapping("/join/{link}")
    public String joinGroup(@PathVariable String link,
                            @RequestParam(required = false) Long productId,  // ← নতুন: URL থেকে productId নাও
                            Principal principal,
                            HttpSession session,
                            RedirectAttributes redirect) {
        System.out.println("Join attempt with link: " + link + ", productId from URL: " + productId);

        if (principal == null || principal.getName() == null) {
            System.out.println("No logged in user - redirecting to login");

            // nextUrl-এ productId যোগ করো যাতে login-এর পর সঠিক গ্রুপে ফিরে আসে
            String nextUrl = "/group-buy/join/" + link;
            if (productId != null) {
                nextUrl += "?productId=" + productId;
            }

            redirect.addFlashAttribute("nextUrl", nextUrl);
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found for username: " + username);
            redirect.addFlashAttribute("error", "ইউজার পাওয়া যায়নি");
            return "redirect:/";
        }

        GroupBuy groupBuy = groupBuyService.findByGroupLink(link).orElse(null);
        if (groupBuy == null) {
            System.out.println("Group not found for link: " + link);
            redirect.addFlashAttribute("error", "গ্রুপ লিঙ্ক ভুল বা মুছে ফেলা হয়েছে");
            return "redirect:/";
        }

        System.out.println("Group found - ID: " + groupBuy.getId() + ", Status: " + groupBuy.getStatus());

        if (groupBuy.getStatus() != GroupBuy.GroupStatus.OPEN) {
            redirect.addFlashAttribute("info", "এই গ্রুপ আর ওপেন নেই");
            return "redirect:/group-buy/" + groupBuy.getProduct().getId();  // ← productId দিয়ে redirect
        }

        boolean joined = groupBuyService.addMember(groupBuy, user);
        if (joined) {
            redirect.addFlashAttribute("success", "আপনি গ্রুপে যোগ দিয়েছেন! 🎉");

            // Cache clear
            session.removeAttribute("groupBuy");

            if (groupBuy.getParticipants().size() >= groupBuy.getRequiredMembers()) {
                groupBuyService.setGroupFull(groupBuy);
                redirect.addFlashAttribute("groupFull", "গ্রুপ পূর্ণ! এখন অর্ডার করতে পারবেন");
            }
        } else {
            redirect.addFlashAttribute("info", "আপনি ইতিমধ্যে এই গ্রুপে যোগ দিয়েছেন");
        }

        System.out.println("Redirecting to group page - Group ID: " + groupBuy.getId() +
                ", Product ID: " + groupBuy.getProduct().getId() + ", Link: " + link);

        // সবসময় সঠিক productId দিয়ে group-buy page-এ ফিরে যাও
        return "redirect:/group-buy/" + groupBuy.getProduct().getId();
    }
    // Order processing
    /*@PostMapping("/order/{groupId}")
    public String orderGroupBuy(@PathVariable Long groupId,
                                Principal principal,
                                HttpSession session,
                                RedirectAttributes redirect) {
        System.out.println("=== orderGroupBuy STARTED === groupId: " + groupId);

        if (principal == null) {
            System.out.println("No authenticated user → redirecting to login");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found: " + username);
            redirect.addFlashAttribute("error", "ইউজার পাওয়া যায়নি");
            return "redirect:/";
        }

        GroupBuy groupBuy = groupBuyService.findById(groupId).orElse(null);
        if (groupBuy == null) {
            System.out.println("GroupBuy not found: " + groupId);
            redirect.addFlashAttribute("error", "গ্রুপ পাওয়া যায়নি");
            return "redirect:/";
        }

        Long productId = groupBuy.getProduct().getId();
        int participantCount = groupBuy.getParticipantCount();
        int currentStock = groupBuy.getProduct().getStock();
        boolean isFull = groupBuy.isFull();

        System.out.println("GroupBuy found - ID: " + groupId +
                ", Product ID: " + productId +
                ", Is Full: " + isFull +
                ", Participants: " + participantCount +
                ", Available Stock: " + currentStock);

        try {
            // প্রথম চেক: গ্রুপ ফুল কি না
            if (!isFull) {
                System.out.println("Group not full (participants: " + participantCount +
                        ", required: " + groupBuy.getRequiredMembers() + ")");
                redirect.addFlashAttribute("error", "গ্রুপ এখনো পূর্ণ হয়নি। " +
                        (groupBuy.getRequiredMembers() - participantCount) +
                        " জন আরও দরকার।");
                return "redirect:/group-buy/" + productId;
            }

            // দ্বিতীয় চেক: স্টক যথেষ্ট কি না (গ্রুপের সবাইকে কভার করার জন্য)
            if (currentStock < participantCount) {
                System.out.println("Insufficient stock - Need: " + participantCount +
                        ", Available: " + currentStock);
                redirect.addFlashAttribute("error", "দুঃখিত! গ্রুপের সকল সদস্যের জন্য প্রোডাক্টের পর্যাপ্ত স্টক নেই। " +
                        "উপলব্ধ: " + currentStock + ", দরকার: " + participantCount);
                return "redirect:/group-buy/" + productId;
            }

            // সব চেক পাস → checkout-এ পাঠাও
            session.setAttribute("groupBuyId", groupId);
            System.out.println("All checks passed → Group full & stock sufficient → Returning checkout page directly");
            return "checkout";  // ← redirect বাদ দিয়ে সরাসরি view render করো

        } catch (Exception e) {
            System.out.println("=== orderGroupBuy CRASHED ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = "অর্ডার প্রক্রিয়াকরণে সমস্যা হয়েছে";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMsg += ": " + e.getMessage();
            }

            redirect.addFlashAttribute("error", errorMsg);
            return "redirect:/group-buy/" + productId;
        }
    }*/



    @PostMapping("/order/{groupId}")
    public String orderGroupBuy(@PathVariable Long groupId,
                                Principal principal,
                                HttpSession session,
                                Model model,  // ← Model parameter যোগ করো (এটা দরকার model পাঠানোর জন্য)
                                RedirectAttributes redirect) {
        System.out.println("=== orderGroupBuy STARTED === groupId: " + groupId);

        if (principal == null) {
            System.out.println("No authenticated user → redirecting to login");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found: " + username);
            redirect.addFlashAttribute("error", "ইউজার পাওয়া যায়নি");
            return "redirect:/";
        }

        GroupBuy groupBuy = groupBuyService.findById(groupId).orElse(null);
        if (groupBuy == null) {
            System.out.println("GroupBuy not found: " + groupId);
            redirect.addFlashAttribute("error", "গ্রুপ পাওয়া যায়নি");
            return "redirect:/";
        }

        Long productId = groupBuy.getProduct().getId();
        int participantCount = groupBuy.getParticipantCount();
        int currentStock = groupBuy.getProduct().getStock();
        boolean isFull = groupBuy.isFull();

        System.out.println("GroupBuy found - ID: " + groupId +
                ", Product ID: " + productId +
                ", Is Full: " + isFull +
                ", Participants: " + participantCount +
                ", Available Stock: " + currentStock);

        try {
            // প্রথম চেক: গ্রুপ ফুল কি না
            if (!isFull) {
                System.out.println("Group not full (participants: " + participantCount +
                        ", required: " + groupBuy.getRequiredMembers() + ")");
                redirect.addFlashAttribute("error", "গ্রুপ এখনো পূর্ণ হয়নি। " +
                        (groupBuy.getRequiredMembers() - participantCount) +
                        " জন আরও দরকার।");
                return "redirect:/group-buy/" + productId;
            }

            // দ্বিতীয় চেক: স্টক যথেষ্ট কি না (গ্রুপের সবাইকে কভার করার জন্য)
            if (currentStock < participantCount) {
                System.out.println("Insufficient stock - Need: " + participantCount +
                        ", Available: " + currentStock);
                redirect.addFlashAttribute("error", "দুঃখিত! গ্রুপের সকল সদস্যের জন্য প্রোডাক্টের পর্যাপ্ত স্টক নেই। " +
                        "উপলব্ধ: " + currentStock + ", দরকার: " + participantCount);
                return "redirect:/group-buy/" + productId;
            }

            // সব চেক পাস → checkout-এ পাঠাও
            session.setAttribute("groupBuyId", groupId);

            // Model-এ groupBuy + total + discount পাঠাও (checkout.html এর জন্য)
            BigDecimal unitPrice = groupBuy.getProduct().getPrice();
            int quantity = groupBuy.getRequiredMembers();
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

            BigDecimal discount = total.multiply(BigDecimal.valueOf(groupBuy.getDiscountPercentage() / 100));
            total = total.subtract(discount);

            model.addAttribute("groupBuy", groupBuy);
            model.addAttribute("total", total);
            model.addAttribute("discountApplied", true);
            model.addAttribute("discountAmount", discount);

            System.out.println("All checks passed → Group full & stock sufficient → Returning checkout page directly");
            System.out.println("Total calculated: " + total + " | Discount: " + discount);

            return "checkout";  // ← redirect বাদ দিয়ে সরাসরি view render করো

        } catch (Exception e) {
            System.out.println("=== orderGroupBuy CRASHED ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = "অর্ডার প্রক্রিয়াকরণে সমস্যা হয়েছে";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMsg += ": " + e.getMessage();
            }

            redirect.addFlashAttribute("error", errorMsg);
            return "redirect:/group-buy/" + productId;
        }
    }



    /*@PostMapping("/order/{groupId}")
    public String orderGroupBuy(@PathVariable Long groupId,
                                Principal principal,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirect) {
        System.out.println("=== orderGroupBuy STARTED === groupId: " + groupId);

        if (principal == null) {
            System.out.println("No authenticated user → redirecting to login");
            return "redirect:/login";
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            System.out.println("User not found: " + username);
            redirect.addFlashAttribute("error", "ইউজার পাওয়া যায়নি");
            return "redirect:/";
        }

        GroupBuy groupBuy = groupBuyService.findById(groupId).orElse(null);
        if (groupBuy == null) {
            System.out.println("GroupBuy not found: " + groupId);
            redirect.addFlashAttribute("error", "গ্রুপ পাওয়া যায়নি");
            return "redirect:/";
        }

        Long productId = groupBuy.getProduct().getId();
        int participantCount = groupBuy.getParticipantCount();
        int currentStock = groupBuy.getProduct().getStock();
        boolean isFull = groupBuy.isFull();

        System.out.println("GroupBuy found - ID: " + groupId +
                ", Product ID: " + productId +
                ", Is Full: " + isFull +
                ", Participants: " + participantCount +
                ", Available Stock: " + currentStock);

        try {
            // প্রথম চেক: গ্রুপ ফুল কি না
            if (!isFull) {
                System.out.println("Group not full (participants: " + participantCount +
                        ", required: " + groupBuy.getRequiredMembers() + ")");
                redirect.addFlashAttribute("error", "গ্রুপ এখনো পূর্ণ হয়নি। " +
                        (groupBuy.getRequiredMembers() - participantCount) +
                        " জন আরও দরকার।");
                return "redirect:/group-buy/" + productId;
            }

            // দ্বিতীয় চেক: স্টক যথেষ্ট কি না
            if (currentStock < participantCount) {
                System.out.println("Insufficient stock - Need: " + participantCount +
                        ", Available: " + currentStock);
                redirect.addFlashAttribute("error", "দুঃখিত! গ্রুপের সকল সদস্যের জন্য প্রোডাক্টের পর্যাপ্ত স্টক নেই। " +
                        "উপলব্ধ: " + currentStock + ", দরকার: " + participantCount);
                return "redirect:/group-buy/" + productId;
            }

            // সব চেক পাস → স্টক কমাও
            Product product = groupBuy.getProduct();
            product.setStock(product.getStock() - participantCount);  // ← স্টক কমানো
            productRepository.save(product);  // ← ডাটাবেসে সেভ করো

            System.out.println("Stock updated successfully! New stock: " + product.getStock());

            // WebSocket-এ স্টক আপডেট পাঠাও (যাতে অন্য ইউজাররা দেখতে পায়)
            messagingTemplate.convertAndSend("/topic/stock/" + productId, product.getStock());

            // checkout-এ পাঠানোর লজিক (তোমার আগের মতোই রাখা)
            session.setAttribute("groupBuyId", groupId);

            BigDecimal unitPrice = groupBuy.getProduct().getPrice();
            int quantity = groupBuy.getRequiredMembers();
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

            BigDecimal discount = total.multiply(BigDecimal.valueOf(groupBuy.getDiscountPercentage() / 100.0));
            total = total.subtract(discount);

            model.addAttribute("groupBuy", groupBuy);
            model.addAttribute("total", total);
            model.addAttribute("discountApplied", true);
            model.addAttribute("discountAmount", discount);

            System.out.println("All checks passed → Proceeding to checkout");
            System.out.println("Total calculated: " + total + " | Discount: " + discount);

            return "checkout";

        } catch (Exception e) {
            System.out.println("=== orderGroupBuy CRASHED ===");
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            String errorMsg = "অর্ডার প্রক্রিয়াকরণে সমস্যা হয়েছে";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMsg += ": " + e.getMessage();
            }

            redirect.addFlashAttribute("error", errorMsg);
            return "redirect:/group-buy/" + productId;
        }
    }*/
}