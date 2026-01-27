package com.example.ecommerce.repository;

import com.example.ecommerce.entity.GroupBuy;
import com.example.ecommerce.entity.GroupBuyMember;
import com.example.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupBuyMemberRepository extends JpaRepository<GroupBuyMember, Long> {
    boolean existsByGroupBuyAndUser(GroupBuy groupBuy, User user);
}