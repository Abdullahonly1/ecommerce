package com.example.ecommerce.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "group_buy_member")
public class GroupBuyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "group_buy_id")
    private GroupBuy groupBuy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GroupBuy getGroupBuy() { return groupBuy; }
    public void setGroupBuy(GroupBuy groupBuy) { this.groupBuy = groupBuy; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}