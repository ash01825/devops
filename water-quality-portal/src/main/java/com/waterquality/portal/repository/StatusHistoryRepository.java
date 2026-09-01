package com.waterquality.portal.repository;

import com.waterquality.portal.domain.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findBySampleIdOrderByChangedAtDesc(Long sampleId);
}
