package com.waterquality.portal.service;

import com.waterquality.portal.domain.Role;
import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import com.waterquality.portal.domain.StatusHistory;
import com.waterquality.portal.repository.SampleRepository;
import com.waterquality.portal.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Central business logic for samples: CRUD, search, role-guarded
 * status workflow with audit history, and dashboard summaries.
 *
 * <p>Workflow: DRAFT -&gt; SUBMITTED -&gt; UNDER_REVIEW -&gt; APPROVED | REJECTED,
 * plus REJECTED -&gt; DRAFT (rework).
 *
 * <p>Transition permissions:
 * <ul>
 *   <li>DRAFT -&gt; SUBMITTED: COLLECTOR, ADMIN</li>
 *   <li>SUBMITTED -&gt; UNDER_REVIEW: ANALYST, REVIEWER, ADMIN</li>
 *   <li>UNDER_REVIEW -&gt; APPROVED: REVIEWER, ADMIN</li>
 *   <li>UNDER_REVIEW -&gt; REJECTED: REVIEWER, ADMIN (comment required)</li>
 *   <li>REJECTED -&gt; DRAFT: COLLECTOR, ADMIN</li>
 * </ul>
 */
@Service
@Transactional
public class SampleService {

    private final SampleRepository sampleRepository;
    private final StatusHistoryRepository historyRepository;

    public SampleService(SampleRepository sampleRepository,
                         StatusHistoryRepository historyRepository) {
        this.sampleRepository = sampleRepository;
        this.historyRepository = historyRepository;
    }

    // ---------- CRUD ----------

    public List<Sample> findAll() {
        return sampleRepository.findAll();
    }

    public Sample findById(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sample not found: " + id));
    }

    public Sample create(Sample sample, String username) {
        if (sample.getStatus() == null) {
            sample.setStatus(SampleStatus.DRAFT);
        }
        if (sampleRepository.findBySampleId(sample.getSampleId()).isPresent()) {
            throw new IllegalArgumentException("Sample ID already exists: " + sample.getSampleId());
        }
        Sample saved = sampleRepository.save(sample);
        recordHistory(saved, null, saved.getStatus(), username, "Sample created");
        return saved;
    }

    public Sample update(Long id, Sample form) {
        Sample existing = findById(id);
        if (existing.getStatus() == SampleStatus.APPROVED) {
            throw new IllegalStateException("Approved samples cannot be edited");
        }
        existing.setStation(form.getStation());
        existing.setCollectedAt(form.getCollectedAt());
        existing.setCollector(form.getCollector());
        existing.setPh(form.getPh());
        existing.setTemperature(form.getTemperature());
        existing.setDissolvedOxygen(form.getDissolvedOxygen());
        existing.setTurbidity(form.getTurbidity());
        existing.setConductivity(form.getConductivity());
        existing.setNotes(form.getNotes());
        // sampleId is immutable once created to keep audit stable
        return sampleRepository.save(existing);
    }

    // ---------- Search ----------

    public List<Sample> search(String q, SampleStatus status, Long stationId,
                               LocalDate from, LocalDate to) {
        LocalDateTime fromDate = (from == null) ? null : from.atStartOfDay();
        LocalDateTime toDate = (to == null) ? null : to.atTime(23, 59, 59);
        return sampleRepository.search(
                (q == null || q.isBlank()) ? null : q.trim(),
                status, stationId, fromDate, toDate);
    }

    // ---------- Workflow ----------

    public Sample transition(Long id, SampleStatus toStatus, String comment, String username, Role role) {
        Sample sample = findById(id);
        SampleStatus from = sample.getStatus();
        assertTransitionAllowed(from, toStatus, role);
        if (toStatus == SampleStatus.REJECTED && (comment == null || comment.isBlank())) {
            throw new IllegalArgumentException("Rejection requires a comment");
        }
        sample.setStatus(toStatus);
        Sample saved = sampleRepository.save(sample);
        recordHistory(saved, from, toStatus, username, comment);
        return saved;
    }

    public static void assertTransitionAllowed(SampleStatus from, SampleStatus to, Role role) {
        Set<Role> allowed = allowedRoles(from, to);
        if (allowed == null) {
            throw new IllegalStateException("Illegal transition: " + from + " -> " + to);
        }
        if (!allowed.contains(role)) {
            throw new SecurityException(
                    "Role " + role + " may not move sample " + from + " -> " + to);
        }
    }

    /** Returns permitted roles for a transition, or null if the transition itself is illegal. */
    public static Set<Role> allowedRoles(SampleStatus from, SampleStatus to) {
        if (from == SampleStatus.DRAFT && to == SampleStatus.SUBMITTED) {
            return Set.of(Role.COLLECTOR, Role.ADMIN);
        }
        if (from == SampleStatus.SUBMITTED && to == SampleStatus.UNDER_REVIEW) {
            return Set.of(Role.ANALYST, Role.REVIEWER, Role.ADMIN);
        }
        if (from == SampleStatus.UNDER_REVIEW && to == SampleStatus.APPROVED) {
            return Set.of(Role.REVIEWER, Role.ADMIN);
        }
        if (from == SampleStatus.UNDER_REVIEW && to == SampleStatus.REJECTED) {
            return Set.of(Role.REVIEWER, Role.ADMIN);
        }
        if (from == SampleStatus.REJECTED && to == SampleStatus.DRAFT) {
            return Set.of(Role.COLLECTOR, Role.ADMIN);
        }
        return null;
    }

    /** Next statuses reachable from the given status (for UI buttons). */
    public static Set<SampleStatus> nextStatuses(SampleStatus from) {
        return switch (from) {
            case DRAFT -> Set.of(SampleStatus.SUBMITTED);
            case SUBMITTED -> Set.of(SampleStatus.UNDER_REVIEW);
            case UNDER_REVIEW -> Set.of(SampleStatus.APPROVED, SampleStatus.REJECTED);
            case REJECTED -> Set.of(SampleStatus.DRAFT);
            case APPROVED -> Set.of();
        };
    }

    // ---------- History ----------

    public List<StatusHistory> historyFor(Long sampleId) {
        return historyRepository.findBySampleIdOrderByChangedAtDesc(sampleId);
    }

    private void recordHistory(Sample sample, SampleStatus from, SampleStatus to,
                               String username, String comment) {
        StatusHistory h = new StatusHistory();
        h.setSample(sample);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setChangedBy(username == null ? "system" : username);
        h.setChangedAt(LocalDateTime.now());
        h.setComment(comment);
        historyRepository.save(h);
    }

    // ---------- Dashboard ----------

    public Map<SampleStatus, Long> countsByStatus() {
        Map<SampleStatus, Long> counts = new EnumMap<>(SampleStatus.class);
        for (SampleStatus s : SampleStatus.values()) {
            counts.put(s, 0L);
        }
        for (Object[] row : sampleRepository.countByStatusGrouped()) {
            counts.put((SampleStatus) row[0], (Long) row[1]);
        }
        return counts;
    }

    public Map<String, Long> countsByStation() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : sampleRepository.countByStationGrouped()) {
            counts.put((String) row[0], (Long) row[1]);
        }
        return counts;
    }

    public long totalCount() {
        return sampleRepository.count();
    }

    public List<Sample> recent(int limit) {
        List<Sample> all = sampleRepository.findAll();
        all.sort((a, b) -> {
            LocalDateTime ca = a.getCreatedAt();
            LocalDateTime cb = b.getCreatedAt();
            if (ca == null && cb == null) return 0;
            if (ca == null) return 1;
            if (cb == null) return -1;
            return cb.compareTo(ca);
        });
        return all.stream().limit(limit).toList();
    }
}
