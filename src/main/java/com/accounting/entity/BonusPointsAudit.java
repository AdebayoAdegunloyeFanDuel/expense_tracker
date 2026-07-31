package com.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonus_points_audit", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BonusPointsAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "spending_id")
    private Long spendingId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private Integer pointsChange;

    @Column(nullable = false)
    private Integer pointsBalanceBefore;

    @Column(nullable = false)
    private Integer pointsBalanceAfter;

    private String category;

    @Column(nullable = false)
    private Boolean rewardTriggered;

    private Long rewardHistoryId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
