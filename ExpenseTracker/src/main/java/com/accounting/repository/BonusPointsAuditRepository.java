package com.accounting.repository;

import com.accounting.entity.BonusPointsAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusPointsAuditRepository extends JpaRepository<BonusPointsAudit, Long> {

    List<BonusPointsAudit> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<BonusPointsAudit> findByUserIdAndSpendingIdOrderByCreatedAtDesc(Long userId, Long spendingId);
}
