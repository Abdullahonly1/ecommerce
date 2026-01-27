package com.example.ecommerce.service;

import com.example.ecommerce.entity.*;
import com.example.ecommerce.repository.GroupBuyMemberRepository;
import com.example.ecommerce.repository.GroupBuyRepository;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupBuyService {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyMemberRepository groupBuyMemberRepository;
    private final ProductRepository productRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // এই মেথড যোগ করো
    public Optional<GroupBuy> findById(Long id) {
        return groupBuyRepository.findById(id);
    }

    @Autowired
    public GroupBuyService(GroupBuyRepository groupBuyRepository,
                           GroupBuyMemberRepository groupBuyMemberRepository,
                           ProductRepository productRepository) {
        this.groupBuyRepository = groupBuyRepository;
        this.groupBuyMemberRepository = groupBuyMemberRepository;
        this.productRepository = productRepository;
    }

    public GroupBuy createGroupBuy(Product product, User creator) {
        System.out.println("createGroupBuy STARTED - productId: " + (product != null ? product.getId() : "NULL") +
                ", creatorId: " + (creator != null ? creator.getId() : "NULL"));

        if (product == null || creator == null) {
            throw new IllegalArgumentException("Product or Creator cannot be null");
        }

        GroupBuy groupBuy = new GroupBuy();
        groupBuy.setProduct(product);
        groupBuy.setCreator(creator);
        groupBuy.setDiscountPercentage(15.0);
        groupBuy.setRequiredMembers(2);
        groupBuy.setStatus(GroupBuy.GroupStatus.OPEN);
        groupBuy.setCreatedAt(LocalDateTime.now());

        // 100% unique short link generate
        String link;
        do {
            link = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        } while (groupBuyRepository.findByGroupLink(link).isPresent());

        groupBuy.setGroupLink(link);
        System.out.println("Generated unique groupLink: " + link);

        // ← নতুন যোগ করা: full join link প্রিন্ট করা (copy-paste টেস্টের জন্য)
        String fullJoinLink = "http://localhost:8080/group-buy/join/" + link + "?productId=" + product.getId();
        System.out.println("Full shareable join link (with productId): " + fullJoinLink);

        try {
            // Save group (participants অটো যোগ হবে যদি CascadeType.ALL থাকে)
            groupBuy = groupBuyRepository.save(groupBuy);

            // Creator-কে অটো participant হিসেবে যোগ করা (যদি entity-তে Cascade না থাকে)
            addMember(groupBuy, creator);

            System.out.println("Group created SUCCESSFULLY - ID: " + groupBuy.getId() +
                    ", Link: " + groupBuy.getGroupLink() +
                    ", Participants: " + groupBuy.getParticipants().size());

            return groupBuy;
        } catch (Exception e) {
            System.out.println("createGroupBuy FAILED: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create group buy", e);
        }
    }

    public Optional<GroupBuy> getGroupBuyByProductAndCreator(Long productId, Long creatorId) {
        System.out.println("getGroupBuyByProductAndCreator called - productId: " + productId + ", creatorId: " + creatorId);

        if (productId == null || creatorId == null) {
            System.out.println("Invalid input - productId or creatorId is null");
            return Optional.empty();
        }

        try {
            String jpql = "SELECT g FROM GroupBuy g WHERE g.product.id = :productId AND g.creator.id = :creatorId";
            return entityManager.createQuery(jpql, GroupBuy.class)
                    .setParameter("productId", productId)
                    .setParameter("creatorId", creatorId)
                    .getResultStream()
                    .findFirst();
        } catch (Exception e) {
            System.out.println("getGroupBuyByProductAndCreator ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean addMember(GroupBuy groupBuy, User user) {
        if (groupBuy == null || user == null) {
            System.out.println("addMember failed - groupBuy or user is null");
            return false;
        }

        // Already joined চেক
        if (groupBuy.getParticipants().stream().anyMatch(p -> p.getUser().getId().equals(user.getId()))) {
            System.out.println("User already in group - ID: " + user.getId());
            return false;
        }

        GroupBuyParticipant participant = new GroupBuyParticipant();
        participant.setGroupBuy(groupBuy);
        participant.setUser(user);
        participant.setJoinedAt(LocalDateTime.now());

        groupBuy.getParticipants().add(participant);

        // Save group (CascadeType.ALL থাকলে participant অটো সেভ হবে)
        groupBuyRepository.save(groupBuy);

        System.out.println("Member added - total participants: " + groupBuy.getParticipants().size());

        if (groupBuy.isFull()) {
            groupBuy.setStatus(GroupBuy.GroupStatus.FULL);
            groupBuyRepository.save(groupBuy);
            System.out.println("Group is now FULL - ID: " + groupBuy.getId());
        }

        return true;
    }

    public GroupBuy getGroupBuyById(Long id) {
        return groupBuyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));
    }

    public Optional<GroupBuy> findByGroupLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            return Optional.empty();
        }
        return groupBuyRepository.findByGroupLink(link);
    }

    public GroupBuy save(GroupBuy groupBuy) {
        return groupBuyRepository.save(groupBuy);
    }

    public void setGroupFull(GroupBuy group) {
        if (group != null) {
            group.setStatus(GroupBuy.GroupStatus.FULL);
            groupBuyRepository.save(group);
        }
    }

    public void processOrder(GroupBuy groupBuy, User user) {
        Product product = groupBuy.getProduct();

        if (product == null) {
            throw new RuntimeException("Product not found in group");
        }

        if (product.getStock() < groupBuy.getRequiredMembers()) {
            throw new RuntimeException("প্রোডাক্টের স্টক যথেষ্ট নেই");
        }

        product.setStock(product.getStock() - groupBuy.getRequiredMembers());
        productRepository.save(product);

        groupBuy.setStatus(GroupBuy.GroupStatus.ORDERED);
        groupBuy.setCompleted(true);
        groupBuyRepository.save(groupBuy);

        System.out.println("Order processed for group ID: " + groupBuy.getId());
    }


    public GroupBuy findCompletedGroupForUserAndProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return null;
        }

        // এই ইউজার এই প্রোডাক্টের কোনো COMPLETED গ্রুপে আছে কি না খুঁজে বের করো
        return groupBuyRepository.findByParticipantsUserIdAndProduct_IdAndStatus(
                userId,
                productId,
                GroupBuy.GroupStatus.COMPLETED
        ).orElse(null);
    }

    // অন্য মেথড যেমন isMember, findCompletedGroupForUserAndProduct থাকলে রাখো
}