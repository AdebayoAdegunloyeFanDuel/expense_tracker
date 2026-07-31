package com.accounting.repository;

import com.accounting.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    Page<Income> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    List<Income> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
        Long userId, LocalDate startDate, LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = :userId")
    BigDecimal getTotalIncomeByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.userId = :userId " +
           "AND i.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncomeByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    void deleteByIdAndUserId(Long incomeId, Long userId);
}
