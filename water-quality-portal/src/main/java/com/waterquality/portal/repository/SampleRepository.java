package com.waterquality.portal.repository;

import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SampleRepository extends JpaRepository<Sample, Long> {

    List<Sample> findByStatus(SampleStatus status);

    List<Sample> findBySampleIdContainingIgnoreCase(String sampleId);
}
