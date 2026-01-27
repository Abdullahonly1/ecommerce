package com.example.ecommerce.controller;

import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.GroupBuy;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.GroupBuyService;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupBuyService groupBuyService;

    @Autowired
    private UserService userService;  // ← Autowired করা হলো (constructor injection-এর বদলে)

    // কার্ট পেজ দেখানো – URL: /cart
    @GetMapping("")
    public String viewCart(Model model, Authentication authentication) {
        List<CartItem> cartItems = cartService.getCartItems(authentication);
        BigDecimal total = cartService.getTotalPrice(authentication);

        cartItems = cartItems != null ? cartItems : new ArrayList<>();
        total = total != null ? total : BigDecimal.ZERO;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        return "cart";
    }

    // কার্ট কাউন্ট – URL: /cart/count
    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(Authentication authentication) {
        return cartService.getCartItemCount(authentication);
    }

    // Add to Cart (AJAX) – URL: /cart/add/{id}
    @PostMapping("/add/{id}")
    @ResponseBody
    public String addToCart(@PathVariable Long id, Authentication authentication, HttpSession session) {
        cartService.addToCart(id, authentication, session);
        return "success";
    }

    // কোয়ান্টিটি আপডেট – URL: /cart/update/{itemId}
    @PostMapping("/update/{itemId}")
    public String updateQuantity(@PathVariable Long itemId, @RequestParam int quantity, Authentication authentication) {
        cartService.updateQuantity(itemId, quantity, authentication);
        return "redirect:/cart";
    }

    // রিমুভ – URL: /cart/remove/{itemId}
    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId, Authentication authentication) {
        cartService.removeItem(itemId, authentication);
        return "redirect:/cart";
    }

    // চেকআউট পেজ – URL: /cart/checkout (GET)
    /*@GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication, HttpSession session) {
        // গ্রুপ বাই থেকে আসলে cart খালি থাকলেও checkout দেখাও
        Long groupBuyId = (Long) session.getAttribute("groupBuyId");
        GroupBuy groupBuy = null;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        boolean discountApplied = false;

        if (groupBuyId != null) {
            groupBuy = groupBuyService.findById(groupBuyId).orElse(null);
            if (groupBuy != null) {
                // গ্রুপ বাই-এর টোটাল ক্যালকুলেট করো
                BigDecimal unitPrice = groupBuy.getProduct().getPrice();
                int quantity = groupBuy.getRequiredMembers();  // গ্রুপের সবাইকে কভার করতে হবে
                total = unitPrice.multiply(BigDecimal.valueOf(quantity));

                // ডিসকাউন্ট অ্যাপ্লাই করো (15% বা যা group-এ আছে)
                BigDecimal discountPercentage = BigDecimal.valueOf(groupBuy.getDiscountPercentage());
                BigDecimal discountFactor = discountPercentage.divide(BigDecimal.valueOf(100));
                discount = total.multiply(discountFactor);
                total = total.subtract(discount);
                discountApplied = true;

                model.addAttribute("groupBuy", groupBuy);
                model.addAttribute("total", total);
                model.addAttribute("discountApplied", discountApplied);
                model.addAttribute("discountAmount", discount);

                // session clear করো যাতে পরেরবার নরমাল কার্ট হিসেবে না নেয়
                session.removeAttribute("groupBuyId");
            }
        }

        // নরমাল কার্ট লজিক (গ্রুপ বাই না থাকলে)
        if (groupBuy == null) {
            if (cartService.getCartItems(authentication).isEmpty()) {
                return "redirect:/cart";
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<CartItem> cartItems = cartService.getCartItems(authentication);
            total = cartService.getTotalPrice(authentication);

            if (!cartItems.isEmpty()) {
                Long productId = cartItems.get(0).getProduct().getId();
                GroupBuy activeGroup = groupBuyService.findCompletedGroupForUserAndProduct(user.getId(), productId);

                if (activeGroup != null && activeGroup.isCompleted()) {
                    BigDecimal discountPercentage = BigDecimal.valueOf(activeGroup.getDiscountPercentage());
                    BigDecimal discountFactor = discountPercentage.divide(BigDecimal.valueOf(100));
                    discount = total.multiply(discountFactor);
                    total = total.subtract(discount);
                    discountApplied = true;
                }
            }

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
            model.addAttribute("discountApplied", discountApplied);
            model.addAttribute("discountAmount", discount);
        }

        return "checkout";
    }*/



    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication, HttpSession session) {
        Long groupBuyId = (Long) session.getAttribute("groupBuyId");
        GroupBuy groupBuy = null;
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;
        boolean discountApplied = false;

        System.out.println("Checkout started - groupBuyId from session: " + groupBuyId); // ← লগ যোগ করো (debug-এর জন্য)

        if (groupBuyId != null) {
            groupBuy = groupBuyService.findById(groupBuyId).orElse(null);
            System.out.println("GroupBuy found: " + (groupBuy != null ? "Yes, ID=" + groupBuy.getId() : "No")); // ← লগ

            if (groupBuy != null && groupBuy.getProduct() != null) {
                BigDecimal unitPrice = groupBuy.getProduct().getPrice();
                int quantity = groupBuy.getRequiredMembers();

                total = unitPrice.multiply(BigDecimal.valueOf(quantity));

                BigDecimal discountPercentage = BigDecimal.valueOf(groupBuy.getDiscountPercentage());
                BigDecimal discountFactor = discountPercentage.divide(BigDecimal.valueOf(100));
                discount = total.multiply(discountFactor);
                total = total.subtract(discount);
                discountApplied = true;

                model.addAttribute("groupBuy", groupBuy);
                model.addAttribute("total", total);
                model.addAttribute("discountApplied", discountApplied);
                model.addAttribute("discountAmount", discount);

                System.out.println("Group buy total calculated: " + total + " | Discount: " + discount); // ← লগ

                // session clear (যাতে পরেরবার নরমাল কার্ট হিসেবে না নেয়)
                session.removeAttribute("groupBuyId");
            } else {
                System.out.println("GroupBuy or Product is null - falling back to normal cart");
            }
        }

        // নরমাল কার্ট লজিক (গ্রুপ বাই না থাকলে বা গ্রুপ না পেলে)
        if (groupBuy == null) {
            List<CartItem> cartItems = cartService.getCartItems(authentication);
            if (cartItems == null || cartItems.isEmpty()) {
                return "redirect:/cart";
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            total = cartService.getTotalPrice(authentication);

            if (!cartItems.isEmpty()) {
                Long productId = cartItems.get(0).getProduct().getId();
                GroupBuy activeGroup = groupBuyService.findCompletedGroupForUserAndProduct(user.getId(), productId);

                if (activeGroup != null && activeGroup.isCompleted()) {
                    BigDecimal discountPercentage = BigDecimal.valueOf(activeGroup.getDiscountPercentage());
                    BigDecimal discountFactor = discountPercentage.divide(BigDecimal.valueOf(100));
                    discount = total.multiply(discountFactor);
                    total = total.subtract(discount);
                    discountApplied = true;
                }
            }

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", total);
            model.addAttribute("discountApplied", discountApplied);
            model.addAttribute("discountAmount", discount);
        }

        return "checkout";
    }

    // Place Order – URL: /cart/checkout (POST)
    @PostMapping("/checkout")
    public String placeOrder(Model model, Authentication authentication, HttpSession session, RedirectAttributes redirect) {
        try {
            System.out.println("=== PLACE ORDER STARTED ===");
            User user = userService.getUserByUsername(authentication.getName());

            // গ্রুপ বাই থেকে আসছে কি না — এটা প্রথমে চেক করো
            Long groupBuyId = (Long) session.getAttribute("groupBuyId");
            boolean isGroupBuyOrder = groupBuyId != null;

            System.out.println("Is group buy order? " + isGroupBuyOrder + " | groupBuyId: " + groupBuyId);

            // গ্রুপ বাই হলে সরাসরি success page-এ যাও (cart চেক skip)
            if (isGroupBuyOrder) {
                System.out.println("Group buy order detected - groupId: " + groupBuyId);
                session.removeAttribute("groupBuyId");

                model.addAttribute("message", "গ্রুপ বাই অর্ডার সফলভাবে প্লেস হয়েছে!");
                System.out.println("RETURNING GROUP-ORDER-SUCCESS PAGE");
                return "group-order-success";  // ← এখানে আসতেই হবে
            }

            // নরমাল কার্টের জন্য চেক
            List<CartItem> cartItems = cartService.getCartItems(authentication);
            System.out.println("Cart items count: " + (cartItems != null ? cartItems.size() : "null"));

            // এই if condition-টা change করো
            if (!isGroupBuyOrder && (cartItems == null || cartItems.isEmpty())) {
                System.out.println("NORMAL CART EMPTY - REDIRECTING TO CART");
                redirect.addFlashAttribute("error", "আপনার কার্ট খালি");
                return "redirect:/cart";
            }

            System.out.println("Processing normal cart - " + cartItems.size() + " items");

            for (CartItem item : cartItems) {
                Long productId = item.getProduct().getId();
                int quantity = item.getQuantity();
                System.out.println("Processing productId: " + productId + ", quantity: " + quantity);
                cartService.processOrder(productId, quantity, authentication, session);
            }

            cartService.clearCart(authentication);
            System.out.println("Cart cleared");

            model.addAttribute("message", "আপনার অর্ডার সফলভাবে প্লেস হয়েছে!");
            System.out.println("RETURNING NORMAL ORDER-SUCCESS PAGE");
            return "order-success";

        } catch (Exception e) {
            System.out.println("=== PLACE ORDER FAILED ===");
            System.out.println("Exception type: " + e.getClass().getSimpleName());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();

            redirect.addFlashAttribute("errorMessage", "অর্ডার প্লেস করতে সমস্যা: " + e.getMessage());
            return "redirect:/cart";
        } finally {
            System.out.println("=== PLACE ORDER ENDED ===");
        }
    }
}