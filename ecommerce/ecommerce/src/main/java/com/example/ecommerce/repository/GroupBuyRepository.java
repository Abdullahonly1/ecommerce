package com.example.ecommerce.repository;

import com.example.ecommerce.entity.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {




    // নতুন কোয়েরি: ইউজারের ID আর প্রোডাক্ট ID দিয়ে কমপ্লিটেড গ্রুপ খুঁজে বের করা
    @Query("SELECT g FROM GroupBuy g JOIN g.participants p " +
            "WHERE p.user.id = :userId AND g.product.id = :productId AND g.isCompleted = true")
    Optional<GroupBuy> findByParticipantsUserIdAndProductIdAndIsCompletedTrue(
            @Param("userId") Long userId,
            @Param("productId") Long productId
    );

    Optional<GroupBuy> findByGroupLink(String groupLink);

    // নতুন কোয়েরি: প্রোডাক্ট ID + ক্রিয়েটর ID দিয়ে গ্রুপ খোঁজা
    Optional<GroupBuy> findByProductIdAndCreatorId(Long productId, Long creatorId);

    Optional<GroupBuy> findByParticipantsUserIdAndProduct_IdAndStatus(
            Long userId,
            Long productId,
            GroupBuy.GroupStatus status
    );
}