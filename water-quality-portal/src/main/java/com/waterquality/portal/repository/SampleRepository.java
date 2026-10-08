package com.waterquality.portal.repository;

import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SampleRepository extends JpaRepository<Sample, Long> {

    Optional<Sample> findBySampleId(String sampleId);

    List<Sample> findByStatus(SampleStatus status);

    List<Sample> findBySampleIdContainingIgnoreCase(String sampleId);

    List<Sample> findByStationId(Long stationId);

    long countByStatus(SampleStatus status);

    @Query("SELECT s FROM Sample s WHERE "
            + "(:q IS NULL OR :q = '' OR LOWER(s.sampleId) LIKE LOWER(CONCAT('%', :q, '%')) "
            + " OR LOWER(s.collector) LIKE LOWER(CONCAT('%', :q, '%')) "
            + " OR LOWER(s.notes) LIKE LOWER(CONCAT('%', :q, '%'))) "
            + "AND (:status IS NULL OR s.status = :status) "
            + "AND (:stationId IS NULL OR s.station.id = :stationId) "
            + "AND (:fromDate IS NULL OR s.collectedAt >= :fromDate) "
            + "AND (:toDate IS NULL OR s.collectedAt <= :toDate) "
            + "ORDER BY s.collectedAt DESC")
    List<Sample> search(@Param("q") String q,
                        @Param("status") SampleStatus status,
                        @Param("stationId") Long stationId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

    @Query("SELECT s.status, COUNT(s) FROM Sample s GROUP BY s.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT s.station.code, COUNT(s) FROM Sample s GROUP BY s.station.code ORDER BY s.station.code")
    List<Object[]> countByStationGrouped();
}
