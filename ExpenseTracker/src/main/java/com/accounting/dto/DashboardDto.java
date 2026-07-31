package com.accounting.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private UserDto user;
    private BigDecimal totalIncome;
    private BigDecimal totalSpending;
    private BigDecimal netBalance;
    private Integer currentBonusPoints;
    private Integer pointsToNextReward;
    private Integer totalRewardsClaimed;
    private List<SpendingDto> recentSpendings;
    private List<IncomeDto> recentIncomes;
    private List<BonusPointsHistoryDto> recentRewards;
}
