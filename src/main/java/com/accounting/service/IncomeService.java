package com.accounting.service;

import com.accounting.dto.CreateIncomeRequest;
import com.accounting.dto.IncomeDto;
import com.accounting.entity.Income;
import com.accounting.repository.IncomeRepository;
import com.accounting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    @Transactional
    public IncomeDto createIncome(Long userId, CreateIncomeRequest request) {
        log.info("Creating income for user={}, source={}", userId, request.getSource());

        // Verify user exists
        userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Income income = Income.builder()
            .userId(userId)
            .source(request.getSource())
            .amount(request.getAmount())
            .transactionDate(request.getTransactionDate())
            .notes(request.getNotes())
            .build();

        income = incomeRepository.save(income);
        log.info("Income created: id={}, amount={}", income.getId(), income.getAmount());

        return mapIncomeToDto(income);
    }

    public List<IncomeDto> getIncomeForUser(Long userId) {
        return incomeRepository.findByUserIdOrderByTransactionDateDesc(userId, null)
            .stream()
            .map(this::mapIncomeToDto)
            .toList();
    }

    @Transactional
    public void deleteIncome(Long userId, Long incomeId) {
        log.info("Deleting income id={} for user={}", incomeId, userId);

        Income income = incomeRepository.findById(incomeId)
            .orElseThrow(() -> new IllegalArgumentException("Income not found"));

        if (!income.getUserId().equals(userId)) {
            throw new SecurityException("User does not own this income record");
        }

        incomeRepository.delete(income);
    }

    private IncomeDto mapIncomeToDto(Income income) {
        return IncomeDto.builder()
            .id(income.getId())
            .userId(income.getUserId())
            .source(income.getSource())
            .amount(income.getAmount())
            .transactionDate(income.getTransactionDate())
            .notes(income.getNotes())
            .createdAt(income.getCreatedAt())
            .build();
    }
}

