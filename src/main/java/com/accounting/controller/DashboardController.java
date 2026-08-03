package com.accounting.controller;

import com.accounting.dto.BonusPointsHistoryDto;
import com.accounting.dto.DashboardDto;
import com.accounting.dto.IncomeDto;
import com.accounting.dto.SpendingDto;
import com.accounting.dto.UserDto;
import com.accounting.service.BonusPointsService;
import com.accounting.service.IncomeService;
import com.accounting.service.SpendingService;
import com.accounting.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final BonusPointsService bonusPointsService;
    private final SpendingService spendingService;
    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(
        @RequestAttribute("userId") Long userId
    ) {
        log.info("Dashboard request for user={}", userId);

        UserDto user = userService.getUserProfile(userId);

        List<SpendingDto> recentSpendings = spendingService.getSpendingForUser(userId)
            .stream()
            .limit(20)
            .toList();

        List<IncomeDto> recentIncomes = incomeService.getIncomeForUser(userId)
            .stream()
            .limit(20)
            .toList();

        List<BonusPointsHistoryDto> recentRewards = bonusPointsService.getPointsHistory(userId)
            .stream()
            .limit(5)
            .toList();

        Integer pointsToNextReward = 15 - user.getBonusPoints();

        DashboardDto dashboard = DashboardDto.builder()
            .user(user)
            .totalIncome(user.getTotalIncome())
            .totalSpending(user.getTotalSpending())
            .netBalance(user.getNetBalance())
            .currentBonusPoints(user.getBonusPoints())
            .pointsToNextReward(pointsToNextReward)
            .totalRewardsClaimed(recentRewards.size())
            .recentSpendings(recentSpendings)
            .recentIncomes(recentIncomes)
            .recentRewards(recentRewards)
            .build();

        return ResponseEntity.ok(dashboard);
    }
}
