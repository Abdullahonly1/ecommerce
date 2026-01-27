package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.ecommerce.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;  // স্টক + ডিটেল লোডের জন্য (যদি না থাকে, মুছে ফেলতে পারো)

    @Autowired
    private UserRepository userRepository;  // ← ৩. এখানে @Autowired দিয়ে ইঞ্জেক্ট করো

    @GetMapping("/")
    public String home(Model model) {
        System.out.println("=== HOME PAGE REQUEST STARTED ===");

        List<Product> products = productRepository.findAll();
        System.out.println("Products loaded: " + (products == null ? "null" : products.size()));

        model.addAttribute("products", products != null ? products : new ArrayList<>());

        System.out.println("=== HOME PAGE REQUEST COMPLETED ===");
        return "index";
    }
    
    @Autowired
    private CartService cartService;
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id,
                                Model model,
                                Authentication authentication,   // ← এটা থাকতে হবে
                                HttpSession session) {           // ← sessionও থাকবে
        Product product = productRepository.findById(id).orElseThrow();

        int stock = cartService.getStockForUser(id, session, authentication);  // ← authentication যোগ করো

        model.addAttribute("product", product);
        model.addAttribute("stock", stock);

        return "product-detail"; // ← এটা change করো (file name product.html হলে "product" রিটার্ন করো)
    }

    // ক্যাটাগরি ক্লিক – URL: /category/Women's Fashion
    @GetMapping("/category/{category}")
    public String categoryProducts(@PathVariable String category, Model model) {
        List<Product> products = productRepository.findByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("title", category + " Products");  // পেজ টাইটেল চেঞ্জ
        model.addAttribute("heading", "All " + category + " Products");
        model.addAttribute("isCategory", true);
        return "index";
    }

    // সার্চ ফর্ম – URL: /search?query=...
    @GetMapping("/search")
    public String searchProducts(@RequestParam String query, Model model) {
        List<Product> products = productRepository.searchProducts(query);
        model.addAttribute("products", products);
        model.addAttribute("title", "Search Results for '" + query + "'");
        model.addAttribute("heading", "Search Results for: " + query);
        model.addAttribute("isSearch", true);
        model.addAttribute("searchQuery", query);
        return "index";
    }

    @GetMapping("/wishlist")
    public String wishlist() {
        return "wishlist"; // templates/wishlist.html
    }


    @GetMapping("/chat")
    public String chat() {
        return "chat";  // templates/chat.html রিটার্ন করবে
    }


    @GetMapping("/myprofile")
    public String showMyProfile(Model model, Authentication authentication) {
        System.out.println("=== /myprofile CALLED ===");

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("No auth → redirect to login");
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        model.addAttribute("user", user);
        return "myprofile";  // ← templates/myprofile.html
    }

    // অথবা যদি আগের /profile থাকে তাহলে এটাও রাখো
    @GetMapping("/profile")
    public String getProfile(Model model, Authentication authentication) {
        return showMyProfile(model, authentication);  // একই লজিক
    }


    /*@GetMapping("/myprofile")
    public String getMyProfile(Model model, Authentication authentication) {
        System.out.println("=== /myprofile CALLED ===");

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        model.addAttribute("user", user);
        return "myprofile";
    }*/



}