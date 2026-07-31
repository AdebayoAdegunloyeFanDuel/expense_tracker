package com.accounting.service;

import com.accounting.dto.BonusPointsHistoryDto;
import com.accounting.dto.BonusPointsResultDto;
import com.accounting.entity.BonusPointsAudit;
import com.accounting.entity.BonusPointsHistory;
import com.accounting.entity.Spending;
import com.accounting.entity.User;
import com.accounting.repository.BonusPointsAuditRepository;
import com.accounting.repository.BonusPointsHistoryRepository;
import com.accounting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BonusPointsService {

    private final UserRepository userRepository;
    private final BonusPointsHistoryRepository bonusPointsHistoryRepository;
    private final BonusPointsAuditRepository bonusPointsAuditRepository;

    public static final Integer POINTS_PER_CAR_CHARGING = 1;
    public static final Integer POINTS_TO_REWARD = 15;
    public static final String CAR_CHARGING_CATEGORY = "Car Charging";

    @Transactional
    public BonusPointsResultDto awardPoints(User user, Spending spending) {
        log.info("Processing bonus points for user={}, spending={}", user.getId(), spending.getId());

        Integer pointsBefore = user.getBonusPoints();
        Integer pointsToAward = 0;
        Boolean rewardTriggered = false;
        BonusPointsHistoryDto rewardDetails = null;

        if (spending.qualifiesForBonusPoints()) {
            pointsToAward = POINTS_PER_CAR_CHARGING;
            user.setBonusPoints(user.getBonusPoints() + pointsToAward);
            log.debug("Points awarded: {} to user {}", pointsToAward, user.getId());
        }

        if (user.getBonusPoints() >= POINTS_TO_REWARD) {
            rewardTriggered = true;

            BonusPointsHistory reward = BonusPointsHistory.builder()
                .userId(user.getId())
                .pointsEarned(POINTS_TO_REWARD)
                .rewardCategory(CAR_CHARGING_CATEGORY)
                .rewardClaimedDate(LocalDateTime.now())
                .build();

            reward = bonusPointsHistoryRepository.save(reward);

            rewardDetails = BonusPointsHistoryDto.builder()
                .id(reward.getId())
                .pointsEarned(reward.getPointsEarned())
                .rewardCategory(reward.getRewardCategory())
                .rewardClaimedDate(reward.getRewardClaimedDate())
                .build();

            user.setBonusPoints(user.getBonusPoints() - POINTS_TO_REWARD);
            log.info("Reward triggered! Points reset. User {} now has {} points",
                     user.getId(), user.getBonusPoints());
        }

        auditPointsChange(
            user.getId(),
            spending.getId(),
            "AWARDED",
            pointsToAward,
            pointsBefore,
            user.getBonusPoints(),
            spending.getCategory(),
            rewardTriggered,
            rewardDetails != null ? rewardDetails.getId() : null
        );

        userRepository.save(user);

        return BonusPointsResultDto.builder()
            .previousPoints(pointsBefore)
            .pointsAwarded(pointsToAward)
            .newPointsBalance(user.getBonusPoints())
            .rewardTriggered(rewardTriggered)
            .rewardDetails(rewardDetails)
            .build();
    }

    @Transactional
    public void reversePoints(User user, Spending spending) {
        log.info("Reversing bonus points for user={}, spending={}", user.getId(), spending.getId());

        if (!spending.qualifiesForBonusPoints()) {
            log.debug("Spending {} does not qualify for bonus points", spending.getId());
            return;
        }

        Integer pointsBefore = user.getBonusPoints();
        Integer pointsToReverse = POINTS_PER_CAR_CHARGING;

        if (pointsBefore > 0) {
            user.setBonusPoints(Math.max(0, pointsBefore - pointsToReverse));
        }

        auditPointsChange(
            user.getId(),
            spending.getId(),
            "REVERSED",
            -pointsToReverse,
            pointsBefore,
            user.getBonusPoints(),
            spending.getCategory(),
            false,
            null
        );

        userRepository.save(user);
    }

    public List<BonusPointsHistoryDto> getPointsHistory(Long userId) {
        List<BonusPointsHistory> history = bonusPointsHistoryRepository
            .findByUserIdOrderByRewardClaimedDateDesc(userId);

        return history.stream()
            .map(h -> BonusPointsHistoryDto.builder()
                .id(h.getId())
                .pointsEarned(h.getPointsEarned())
                .rewardCategory(h.getRewardCategory())
                .rewardClaimedDate(h.getRewardClaimedDate())
                .build())
            .toList();
    }

    public List<BonusPointsAudit> getAuditTrail(Long userId) {
        return bonusPointsAuditRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private void auditPointsChange(Long userId, Long spendingId, String action,
                                   Integer pointsChange, Integer pointsBefore,
                                   Integer pointsAfter, String category,
                                   Boolean rewardTriggered, Long rewardHistoryId) {
        BonusPointsAudit audit = BonusPointsAudit.builder()
            .userId(userId)
            .spendingId(spendingId)
            .action(action)
            .pointsChange(pointsChange)
            .pointsBalanceBefore(pointsBefore)
            .pointsBalanceAfter(pointsAfter)
            .category(category)
            .rewardTriggered(rewardTriggered)
            .rewardHistoryId(rewardHistoryId)
            .build();

        bonusPointsAuditRepository.save(audit);
    }
}
