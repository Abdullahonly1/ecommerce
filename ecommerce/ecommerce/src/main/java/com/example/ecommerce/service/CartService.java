package com.example.ecommerce.service;

import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.websocket.StockUpdateHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final StockUpdateHandler stockUpdateHandler;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository,
                       StockUpdateHandler stockUpdateHandler, UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.stockUpdateHandler = stockUpdateHandler;
        this.userRepository = userRepository;
    }

    /*@Async
    @Transactional
    public void addToCart(Long productId, Authentication authentication, HttpSession session) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return;

        Product product = productOpt.get();

        synchronized (productId.toString().intern()) {
            // সেশনে টেম্পোরারি লক সেট করো (শুধু ঐ ইউজারের কাছে out of stock দেখাবে)
            session.setAttribute("tempLocked_" + productId, true);

            // নতুন যোগ: stock চেক করো
            if (product.getStock() <= 0) {
                session.removeAttribute("tempLocked_" + productId);
                throw new RuntimeException("Product out of stock");
            }

            Optional<CartItem> existing = cartItemRepository.findByUserAndProduct(user, product);

            int quantityToAdd = 1;
            if (existing.isPresent()) {
                CartItem item = existing.get();
                quantityToAdd = item.getQuantity() + 1;
                item.setQuantity(quantityToAdd);
                cartItemRepository.save(item);
            } else {
                CartItem newItem = new CartItem(product, user);
                cartItemRepository.save(newItem);
            }

            // ডাটাবেসে স্টক কমাবে না — শুধু ইউজার-স্পেসিফিক view-এ কম দেখাবে
            // রিয়েল-টাইম আপডেট (অন্য ইউজারের কাছে পুরোনো stock দেখাবে)
            stockUpdateHandler.broadcastStockUpdate(productId, product.getStock());
        }
    }*/


    @Async
    @Transactional
    public void addToCart(Long productId, Authentication authentication, HttpSession session) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return;

        Product product = productOpt.get();

        synchronized (productId.toString().intern()) {
            // Session-এ temp lock সেট করো — এটা getStockForUser()-এ ব্যবহার হবে
            session.setAttribute("tempLocked_" + productId, true);

            // Cart item যোগ/আপডেট
            Optional<CartItem> existing = cartItemRepository.findByUserAndProduct(user, product);

            if (existing.isPresent()) {
                CartItem item = existing.get();
                item.setQuantity(item.getQuantity() + 1);
                cartItemRepository.save(item);
            } else {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setUser(user);
                newItem.setQuantity(1);
                newItem.setPrice(product.getPrice());
                cartItemRepository.save(newItem);
            }

            // রিয়েল-টাইম স্টক আপডেট (যদি WebSocket থাকে)
            stockUpdateHandler.broadcastStockUpdate(productId, product.getStock());
        }
    }

    @Transactional
    public void updateQuantity(Long itemId, int newQuantity, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<CartItem> itemOpt = cartItemRepository.findByIdAndUser(itemId, user);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else if (itemOpt.isPresent() && newQuantity <= 0) {
            cartItemRepository.delete(itemOpt.get());
        }
    }

    @Transactional
    public void removeItem(Long itemId, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<CartItem> itemOpt = cartItemRepository.findByIdAndUser(itemId, user);
        itemOpt.ifPresent(cartItemRepository::delete);
    }

    public List<CartItem> getCartItems(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            List<CartItem> items = cartItemRepository.findByUser(user);
            if (items != null) {
                for (CartItem item : items) {
                    if (item.getProduct() != null) {
                        item.getProduct().getImageUrl();
                        item.getProduct().getName();
                        item.getProduct().getStock();
                    }
                }
            }
            return items != null ? items : new ArrayList<>();
        } catch (Exception e) {
            System.out.println("Error loading cart: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public BigDecimal getTotalPrice(Authentication authentication) {
        List<CartItem> items = getCartItems(authentication);
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item != null && item.getPrice() != null && item.getQuantity() != null) {
                sum = sum.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return sum;
    }

    public int getCartItemCount(Authentication authentication) {
        return getCartItems(authentication).stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Transactional
    public void clearCart(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        cartItemRepository.deleteByUser(user);
    }

    @Async
    public void processOrder(Long productId, int quantityToReduce, Authentication authentication, HttpSession session) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        synchronized (productId.toString().intern()) {
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                if (product.getStock() >= quantityToReduce) {
                    product.setStock(product.getStock() - quantityToReduce);
                    productRepository.save(product);
                    stockUpdateHandler.broadcastStockUpdate(productId, product.getStock());
                } else {
                    System.out.println("স্টক যথেষ্ট নেই: " + product.getStock() + " < " + quantityToReduce);
                }
            }
        }

        session.removeAttribute("tempLocked_" + productId);
    }

    // ইউজার-স্পেসিফিক স্টক দেখানোর লজিক
    /*public int getStockForUser(Long productId, HttpSession session, Authentication authentication) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        // লগইন না থাকলে পুরো stock
        if (authentication == null) {
            return product.getStock();
        }

        // ইউজারের কার্টে কতটা আছে চেক করো
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        int cartQuantity = 0;
        if (user != null) {
            Optional<CartItem> cartItem = cartItemRepository.findByUserAndProduct(user, product);
            cartQuantity = cartItem.isPresent() ? cartItem.get().getQuantity() : 0;
        }

        // ইউজারের কাছে cart quantity কমিয়ে দেখাবে
        int availableStock = product.getStock() - cartQuantity;
        return availableStock > 0 ? availableStock : 0;
    }*/



    public int getStockForUser(Long productId, HttpSession session, Authentication authentication) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

        // লগইন না থাকলে পুরো stock
        if (authentication == null) {
            return product.getStock();
        }

        // ইউজারের session-এ temp lock চেক করো
        Boolean isTempLocked = (Boolean) session.getAttribute("tempLocked_" + productId);

        if (isTempLocked != null && isTempLocked) {
            // ঐ ইউজারের কার্টে কতটা আছে
            User user = userRepository.findByUsername(authentication.getName()).orElse(null);
            int cartQuantity = 0;
            if (user != null) {
                Optional<CartItem> cartItem = cartItemRepository.findByUserAndProduct(user, product);
                cartQuantity = cartItem.isPresent() ? cartItem.get().getQuantity() : 0;
            }

            // পুরো stock থেকে cart quantity কমিয়ে দেখাও
            int availableStock = product.getStock() - cartQuantity;
            return availableStock > 0 ? availableStock : 0;
        } else {
            return product.getStock(); // অন্য ইউজার বা lock না থাকলে পুরো stock
        }
    }
}