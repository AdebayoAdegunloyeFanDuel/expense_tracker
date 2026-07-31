package com.accounting.controller;

import com.accounting.dto.CreateIncomeRequest;
import com.accounting.dto.IncomeDto;
import com.accounting.dto.PagedResponse;
import com.accounting.service.IncomeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/income")
@RequiredArgsConstructor
@Validated
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDto> createIncome(
        @RequestAttribute("userId") Long userId,
        @Valid @RequestBody CreateIncomeRequest request
    ) {
        log.info("Create income request for user={}", userId);
        IncomeDto income = incomeService.createIncome(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(income);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<IncomeDto>> getIncome(
        @RequestAttribute("userId") Long userId
    ) {
        log.info("Get income request for user={}", userId);
        List<IncomeDto> incomes = incomeService.getIncomeForUser(userId);

        PagedResponse<IncomeDto> response = PagedResponse.<IncomeDto>builder()
            .content(incomes)
            .page(1)
            .pageSize(incomes.size())
            .totalElements((long) incomes.size())
            .totalPages(1)
            .isLast(true)
            .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<Void> deleteIncome(
        @RequestAttribute("userId") Long userId,
        @PathVariable @NotNull Long incomeId
    ) {
        log.info("Delete income request: user={}, incomeId={}", userId, incomeId);
        incomeService.deleteIncome(userId, incomeId);
        return ResponseEntity.noContent().build();
    }
}

