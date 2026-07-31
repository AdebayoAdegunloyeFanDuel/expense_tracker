package com.accounting.service;

import com.accounting.dto.RegisterRequest;
import com.accounting.dto.UserDto;
import com.accounting.entity.User;
import com.accounting.repository.BonusPointsHistoryRepository;
import com.accounting.repository.IncomeRepository;
import com.accounting.repository.SpendingRepository;
import com.accounting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final SpendingRepository spendingRepository;
    private final BonusPointsHistoryRepository bonusPointsHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .bonusPoints(0)
            .build();

        user = userRepository.save(user);
        return mapUserToDto(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserDto getUserProfile(Long userId) {
        User user = getUserById(userId);

        BigDecimal totalIncome = incomeRepository.getTotalIncomeByUserId(userId);
        BigDecimal totalSpending = spendingRepository.getTotalSpendingByUserId(userId);
        BigDecimal netBalance = totalIncome.subtract(totalSpending);

        return UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .bonusPoints(user.getBonusPoints())
            .totalIncome(totalIncome)
            .totalSpending(totalSpending)
            .netBalance(netBalance)
            .createdAt(user.getCreatedAt())
            .build();
    }

    private UserDto mapUserToDto(User user) {
        return getUserProfile(user.getId());
    }
}
