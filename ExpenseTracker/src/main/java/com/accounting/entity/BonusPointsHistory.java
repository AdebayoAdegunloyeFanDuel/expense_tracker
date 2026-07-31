package com.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_points_history", indexes = {
    @Index(name = "idx_user_reward", columnList = "user_id,reward_claimed_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusPointsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer pointsEarned = 15;

    @Column(nullable = false)
    private String rewardCategory;

    @Column(name = "reward_claimed_date", nullable = false)
    private LocalDateTime rewardClaimedDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (rewardClaimedDate == null) {
            rewardClaimedDate = LocalDateTime.now();
        }
    }
}
