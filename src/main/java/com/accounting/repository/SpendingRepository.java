package com.accounting.repository;

import com.accounting.entity.Spending;
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
public interface SpendingRepository extends JpaRepository<Spending, Long> {

    Page<Spending> findByUserIdOrderByTransactionDateDesc(Long userId, Pageable pageable);

    List<Spending> findByUserIdAndCategoryOrderByTransactionDateDesc(Long userId, String category);

    List<Spending> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
        Long userId, LocalDate startDate, LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Spending s WHERE s.userId = :userId")
    BigDecimal getTotalSpendingByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Spending s WHERE s.userId = :userId " +
           "AND s.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSpendingByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(s) FROM Spending s WHERE s.userId = :userId AND s.category = :category")
    Integer countSpendingByUserIdAndCategory(
        @Param("userId") Long userId,
        @Param("category") String category
    );

    List<Spending> findByUserIdAndCategoryOrderByCreatedAtDesc(Long userId, String category);

    void deleteByIdAndUserId(Long spendingId, Long userId);
}
