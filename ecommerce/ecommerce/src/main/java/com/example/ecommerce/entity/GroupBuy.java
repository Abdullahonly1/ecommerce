package com.example.ecommerce.entity;
/*
import com.example.ecommerce.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_buy")
@Getter
public class GroupBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private int requiredMembers = 4; // কতজন লাগবে (ডিফল্ট ৪)

    private double discountPercentage = 15.0; // ডিসকাউন্ট %

    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyParticipant> participants = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.OPEN;

    //private String groupLink; // random link for sharing
    @Column(name = "group_link", nullable = true, unique = true)
    private String groupLink;

    public enum GroupStatus {
        OPEN, FULL, ORDERED, CANCELLED
    }


    // কনস্ট্রাক্টর
    public GroupBuy() {}

    public GroupBuy(Product product, User creator) {
        this.product = product;
        this.creator = creator;
        this.participants.add(new GroupBuyParticipant(this, creator)); // creator নিজে যোগ হয়
    }

    // isFull() মেথড – participants.size() দিয়ে চেক
    public boolean isFull() {
        return participants.size() >= requiredMembers;
    }

    // যদি Lombok না কাজ করে তাহলে এই ম্যানুয়াল getter যোগ করো (অন্যথায় বাদ দিতে পারো)
    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public User getCreator() {
        return creator;
    }

    public int getRequiredMembers() {
        return requiredMembers;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public List<GroupBuyParticipant> getParticipants() {
        return participants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public GroupStatus getStatus() {
        return status;
    }

    public String getGroupLink() {
        return groupLink;
    }

    // setter যদি Lombok না থাকে তাহলে যোগ করো (অন্যথায় বাদ দিতে পারো)
    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void setRequiredMembers(int requiredMembers) {
        this.requiredMembers = requiredMembers;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public void setParticipants(List<GroupBuyParticipant> participants) {
        this.participants = participants;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setStatus(GroupStatus status) {
        this.status = status;
    }

    public void setGroupLink(String groupLink) {
        this.groupLink = groupLink;
    }

    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    public Long getCreatorId() {
        return creator != null ? creator.getId() : null;
    }




    // যদি size() সহজে পেতে চাও (পেজে কাউন্ট দেখানোর জন্য)
    public int getParticipantCount() {
        return participants != null ? participants.size() : 0;
    }
}*/


import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_buy")
@Getter
public class GroupBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private int requiredMembers = 3; // কতজন লাগবে (ডিফল্ট ৪)

    private double discountPercentage = 15.0; // ডিসকাউন্ট %

    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyParticipant> participants = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.OPEN;

    @Column(name = "group_link", nullable = true, unique = true)
    private String groupLink;

    public enum GroupStatus {
        OPEN, FULL, ORDERED,COMPLETED, CANCELLED
    }


    @Transient  // DB-এ save হবে না, শুধু link-এ ব্যবহারের জন্য
    private String fullJoinLink;

    @Transient
    public String getFullJoinLink() {
        if (this.product == null || this.product.getId() == null) {
            return "http://localhost:8080/group-buy/join/" + this.groupLink;
        }
        return "http://localhost:8080/group-buy/join/" + this.groupLink + "?productId=" + this.product.getId();
    }



    // কনস্ট্রাক্টর
    public GroupBuy() {}

    public GroupBuy(Product product, User creator) {
        this.product = product;
        this.creator = creator;
        this.participants.add(new GroupBuyParticipant(this, creator)); // creator নিজে যোগ হয়
    }

    // isFull() মেথড
    public boolean isFull() {
        return participants.size() >= requiredMembers;
    }

    // Hibernate mapping-এর বাইরে রাখার জন্য @Transient দাও
    @Transient // ← এটা যোগ করো — Hibernate এটাকে DB column হিসেবে দেখবে না
    public Long getCreatorId() {
        return creator != null ? creator.getId() : null;
    }

    @Transient // ← এটাও যোগ করো
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }

    // যদি participant count সহজে পেতে চাও
    @Transient
    public int getParticipantCount() {
        return participants != null ? participants.size() : 0;
    }

    // setters (যদি Lombok @Getter/@Setter ব্যবহার না করো তাহলে যোগ করো)
    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void setRequiredMembers(int requiredMembers) {
        this.requiredMembers = requiredMembers;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public void setParticipants(List<GroupBuyParticipant> participants) {
        this.participants = participants;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setStatus(GroupStatus status) {
        this.status = status;
    }

    public void setGroupLink(String groupLink) {
        this.groupLink = groupLink;
    }



}