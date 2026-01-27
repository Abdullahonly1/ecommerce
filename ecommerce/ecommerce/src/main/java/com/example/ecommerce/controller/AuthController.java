package com.example.ecommerce.controller;

/*
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@Controller
public class AuthController {
    private final UserService userService;  // ← এটা যোগ করো

    // constructor injection (সবচেয়ে ভালো প্র্যাকটিস)
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // registration form-এর জন্য empty User object
        return "register"; // templates/register.html পেজ রিটার্ন করবে
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               RedirectAttributes redirect) {
        // validation + save logic
        userService.registerUser(user);
        redirect.addFlashAttribute("success", "রেজিস্ট্রেশন সফল! লগইন করুন");
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model, RedirectAttributes redirect) {
        System.out.println("Register attempt - username: " + username + ", email: " + email);

        try {
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "Username is required!");
                return "register";
            }
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required!");
                return "register";
            }
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Password is required!");
                return "register";
            }

            if (userRepository.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Username already exists!");
                return "register";
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password)); // BCrypt encode

            System.out.println("Saving user: " + username);
            userRepository.save(user);
            System.out.println("User saved successfully: " + username);

            redirect.addFlashAttribute("success", "রেজিস্ট্রেশন সফল! এখন লগইন করুন।");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("REGISTER ERROR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace(); // পুরো স্ট্যাক ট্রেস দেখাবে
            model.addAttribute("error", "রেজিস্ট্রেশন ফেল হয়েছে: " + e.getMessage());
            return "register";
        }
    }

    /*@GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "nextUrl", required = false) String nextUrl,
                                Model model) {
        // লগইন পেজে nextUrl পাঠানো যাতে লগইনের পর ফিরে যেতে পারে
        if (nextUrl != null && !nextUrl.isEmpty()) {
            model.addAttribute("nextUrl", nextUrl);
        }
        return "login";
    }*/

/*
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "nextUrl", required = false) String nextUrl,
                                HttpSession session,
                                Model model) {
        if (nextUrl != null && !nextUrl.isEmpty()) {
            // Mock request তৈরি করো
            MockHttpServletRequest mockRequest = new MockHttpServletRequest();
            mockRequest.setRequestURI(nextUrl);
            mockRequest.setMethod("GET");
            mockRequest.setServerName("localhost");
            mockRequest.setServerPort(8080);
            mockRequest.setContextPath("");

            // DefaultSavedRequest তৈরি করো — দ্বিতীয় প্যারামিটার হবে contextPath (String)
            DefaultSavedRequest savedRequest = new DefaultSavedRequest(mockRequest, "");

            // session-এ সেভ করো
            session.setAttribute("SPRING_SECURITY_SAVED_REQUEST", savedRequest);
            model.addAttribute("nextUrl", nextUrl);
            System.out.println("Saved nextUrl as SavedRequest object: " + nextUrl);
        } else {
            session.removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            System.out.println("No nextUrl - cleared");
        }

        return "login";
    }

    // Spring Security অটো লগইন হ্যান্ডল করে, কিন্তু সাকসেসের পর nextUrl চেক করার জন্য
    // কাস্টম success handler যোগ করতে হবে (যদি Security Config-এ না থাকে)
    // অথবা Security Config-এ successHandler যোগ করো (নিচে দেখো)
}


*/






import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection (সবচেয়ে ভালো প্র্যাকটিস)
    public AuthController(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // রেজিস্ট্রেশন ফর্ম দেখানো
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // empty User object for form
        return "register"; // templates/register.html
    }

    // রেজিস্ট্রেশন প্রসেস (POST)
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               RedirectAttributes redirect) {
        try {
            // basic validation
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                redirect.addFlashAttribute("error", "Username is required!");
                return "redirect:/register";
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                redirect.addFlashAttribute("error", "Email is required!");
                return "redirect:/register";
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                redirect.addFlashAttribute("error", "Password is required!");
                return "redirect:/register";
            }

            // username/email already exists চেক
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                redirect.addFlashAttribute("error", "Username already exists!");
                return "redirect:/register";
            }
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                redirect.addFlashAttribute("error", "Email already exists!");
                return "redirect:/register";
            }

            // password encode + save
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.registerUser(user); // তোমার service-এ save logic যদি থাকে

            redirect.addFlashAttribute("success", "রেজিস্ট্রেশন সফল! এখন লগইন করুন।");
            return "redirect:/login";

        } catch (Exception e) {
            System.out.println("REGISTER ERROR: " + e.getMessage());
            e.printStackTrace();
            redirect.addFlashAttribute("error", "রেজিস্ট্রেশন ফেল হয়েছে: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // লগইন ফর্ম দেখানো + nextUrl সেভ করা
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "nextUrl", required = false) String nextUrl,
                                HttpSession session,
                                Model model) {
        if (nextUrl != null && !nextUrl.isEmpty()) {
            // শুধু String হিসেবে সেভ করো
            session.setAttribute("customNextUrl", nextUrl);
            model.addAttribute("nextUrl", nextUrl);
            System.out.println("Saved nextUrl as String: " + nextUrl);
        } else {
            session.removeAttribute("customNextUrl");
            System.out.println("No nextUrl - cleared");
        }

        return "login";
    }
}