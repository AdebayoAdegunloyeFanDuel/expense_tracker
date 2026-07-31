package com.accounting.controller;

import com.accounting.dto.CreateSpendingRequest;
import com.accounting.dto.PagedResponse;
import com.accounting.dto.SpendingDto;
import com.accounting.service.SpendingService;
import com.accounting.service.UserService;
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
@RequestMapping("/spending")
@RequiredArgsConstructor
@Validated
public class SpendingController {

    private final SpendingService spendingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<SpendingDto> createSpending(
        @RequestAttribute("userId") Long userId,
        @Valid @RequestBody CreateSpendingRequest request
    ) {
        log.info("Create spending request for user={}", userId);
        SpendingDto spending = spendingService.createSpending(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(spending);
    }

    @DeleteMapping("/{spendingId}")
    public ResponseEntity<Void> deleteSpending(
        @RequestAttribute("userId") Long userId,
        @PathVariable @NotNull Long spendingId
    ) {
        log.info("Delete spending request: user={}, spendingId={}", userId, spendingId);
        spendingService.deleteSpending(userId, spendingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<SpendingDto>> getSpending(
        @RequestAttribute("userId") Long userId
    ) {
        log.info("Get spending request for user={}", userId);
        List<SpendingDto> spendings = spendingService.getSpendingForUser(userId);

        PagedResponse<SpendingDto> response = PagedResponse.<SpendingDto>builder()
            .content(spendings)
            .page(1)
            .pageSize(spendings.size())
            .totalElements((long) spendings.size())
            .totalPages(1)
            .isLast(true)
            .build();

        return ResponseEntity.ok(response);
    }
}
