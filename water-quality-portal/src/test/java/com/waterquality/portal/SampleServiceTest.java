package com.waterquality.portal;

import com.waterquality.portal.domain.Role;
import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import com.waterquality.portal.domain.Station;
import com.waterquality.portal.repository.StationRepository;
import com.waterquality.portal.service.SampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SampleServiceTest {

    @Autowired
    SampleService samples;

    @Autowired
    StationRepository stations;

    private Sample newSample(String id) {
        Station st = stations.findAll().get(0);
        Sample s = new Sample();
        s.setSampleId(id);
        s.setStation(st);
        s.setCollectedAt(LocalDateTime.now().minusHours(2));
        s.setCollector("Test Collector");
        return s;
    }

    @Test
    void createStartsInDraftAndRecordsHistory() {
        Sample saved = samples.create(newSample("WQ-T-001"), "collector");
        assertEquals(SampleStatus.DRAFT, saved.getStatus());
        assertFalse(samples.historyFor(saved.getId()).isEmpty());
    }

    @Test
    void duplicateSampleIdIsRejected() {
        samples.create(newSample("WQ-T-002"), "collector");
        assertThrows(IllegalArgumentException.class,
                () -> samples.create(newSample("WQ-T-002"), "collector"));
    }

    @Test
    void fullWorkflowSucceedsWithRightRoles() {
        Sample s = samples.create(newSample("WQ-T-003"), "collector");
        samples.transition(s.getId(), SampleStatus.SUBMITTED, "submit", "collector", Role.COLLECTOR);
        samples.transition(s.getId(), SampleStatus.UNDER_REVIEW, "take", "analyst", Role.ANALYST);
        samples.transition(s.getId(), SampleStatus.APPROVED, "looks good", "reviewer", Role.REVIEWER);
        assertEquals(SampleStatus.APPROVED, samples.findById(s.getId()).getStatus());
        assertEquals(4, samples.historyFor(s.getId()).size());
    }

    @Test
    void wrongRoleIsRejected() {
        Sample s = samples.create(newSample("WQ-T-004"), "collector");
        assertThrows(SecurityException.class, () -> samples.transition(
                s.getId(), SampleStatus.SUBMITTED, "x", "analyst", Role.ANALYST));
    }

    @Test
    void illegalTransitionIsRejected() {
        Sample s = samples.create(newSample("WQ-T-005"), "collector");
        assertThrows(IllegalStateException.class, () -> samples.transition(
                s.getId(), SampleStatus.APPROVED, "skip", "admin", Role.ADMIN));
    }

    @Test
    void rejectionRequiresComment() {
        Sample s = samples.create(newSample("WQ-T-006"), "collector");
        samples.transition(s.getId(), SampleStatus.SUBMITTED, "s", "collector", Role.COLLECTOR);
        samples.transition(s.getId(), SampleStatus.UNDER_REVIEW, "t", "reviewer", Role.REVIEWER);
        assertThrows(IllegalArgumentException.class, () -> samples.transition(
                s.getId(), SampleStatus.REJECTED, " ", "reviewer", Role.REVIEWER));
    }

    @Test
    void searchFiltersByKeywordAndStatus() {
        samples.create(newSample("WQ-T-010"), "collector");
        assertFalse(samples.search("WQ-T-010", null, null, null, null).isEmpty());
        assertTrue(samples.search("NO-SUCH-SAMPLE-XYZ", null, null, null, null).isEmpty());
        assertFalse(samples.search(null, SampleStatus.DRAFT, null, null, null).isEmpty());
    }

    @Test
    void dashboardCountsAreConsistent() {
        long before = samples.totalCount();
        samples.create(newSample("WQ-T-020"), "collector");
        assertEquals(before + 1, samples.totalCount());
        Map<SampleStatus, Long> counts = samples.countsByStatus();
        long sum = counts.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(samples.totalCount(), sum);
    }
}
