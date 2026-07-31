package com.accounting.repository;

import com.accounting.entity.BonusPointsHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BonusPointsHistoryRepository extends JpaRepository<BonusPointsHistory, Long> {

    List<BonusPointsHistory> findByUserIdOrderByRewardClaimedDateDesc(Long userId);

    Page<BonusPointsHistory> findByUserIdOrderByRewardClaimedDateDesc(Long userId, Pageable pageable);

    Integer countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(bph.pointsEarned), 0) FROM BonusPointsHistory bph WHERE bph.userId = :userId")
    Integer getTotalPointsEarnedByUserId(@Param("userId") Long userId);

    List<BonusPointsHistory> findByUserIdAndRewardClaimedDateBetweenOrderByRewardClaimedDateDesc(
        Long userId, LocalDateTime startDate, LocalDateTime endDate
    );
}
