package com.example.ecommerce.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(Exception.class)
    public String handleAllExceptions(Exception ex, Model model, HttpServletRequest request) {
        String timestamp = LocalDateTime.now().format(formatter);
        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        // লগিং — কনসোলে পরিষ্কার দেখা যাবে
        System.out.println("[" + timestamp + "] GLOBAL EXCEPTION CAUGHT");
        System.out.println("URL: " + method + " " + url + query);
        System.out.println("Exception Type: " + ex.getClass().getName());
        System.out.println("Message: " + (ex.getMessage() != null ? ex.getMessage() : "No message"));
        System.out.println("Stack Trace (first 5 lines):");

        // শুধু প্রথম ৫ লাইন stack trace প্রিন্ট (console clean রাখতে)
        StackTraceElement[] stack = ex.getStackTrace();
        for (int i = 0; i < Math.min(5, stack.length); i++) {
            System.out.println("  at " + stack[i]);
        }
        if (stack.length > 5) {
            System.out.println("  ... (more stack trace omitted)");
        }
        ex.printStackTrace(); // পুরো trace চাইলে এটা রাখো, না চাইলে কমেন্ট করো

        // Model-এ ইউজার-ফ্রেন্ডলি ডেটা পাঠানো
        model.addAttribute("timestamp", timestamp);
        model.addAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : "একটি অপ্রত্যাশিত সমস্যা হয়েছে। দয়া করে পরে আবার চেষ্টা করুন।");
        model.addAttribute("errorType", ex.getClass().getSimpleName());
        model.addAttribute("url", url + query);

        // error.html পেজে ফেরত পাঠানো
        return "error";
    }

    // Optional: NullPointerException আলাদা হ্যান্ডেল (যদি চাও)
    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointer(NullPointerException ex, Model model, HttpServletRequest request) {
        model.addAttribute("timestamp", LocalDateTime.now().format(formatter));
        model.addAttribute("errorMessage", "কোনো ডেটা পাওয়া যায়নি (Null Pointer Exception)। দয়া করে পেজ রিফ্রেশ করুন বা সাপোর্টে যোগাযোগ করুন।");
        model.addAttribute("errorType", "NullPointerException");
        model.addAttribute("url", request.getRequestURL().toString());

        return "error";
    }

    // আরও নির্দিষ্ট exception চাইলে এখানে যোগ করতে পারো
}