package com.accounting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Audit table for tracking every bonus point change
 * Useful for debugging point calculation issues and testing
 * 
 * This is OPTIONAL for the MVP but highly recommended because:
 * 1. You can replay point calculations to verify correctness
 * 2. Easy to find bugs in point logic
 * 3. Essential for multi-threaded testing
 */
@Entity
@Table(name = "bonus_points_audit", indexes = {
    @Index(name = "idx_audit_user_date", columnList = "user_id,created_at"),
    @Index(name = "idx_audit_spending_id", columnList = "spending_id")
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
    
    @Column(name = "spending_id", nullable = false)
    private Long spendingId;
    
    @Column(nullable = false)
    private String action;  // AWARDED, REVERSED, RESET
    
    @Column(nullable = false)
    private Integer pointsChange;  // Can be negative (reversal)
    
    @Column(nullable = false)
    private Integer pointsBalanceBefore;
    
    @Column(nullable = false)
    private Integer pointsBalanceAfter;
    
    @Column(nullable = false)
    private String category;  // The category of the spending
    
    @Column(nullable = false)
    private Boolean rewardTriggered = false;
    
    private Long rewardHistoryId;  // If reward was triggered, reference to BonusPointsHistory
    
    @Column(columnDefinition = "TEXT")
    private String notes;  // Any additional context (error messages, etc.)
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
