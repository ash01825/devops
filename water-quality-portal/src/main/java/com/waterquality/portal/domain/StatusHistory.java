package com.waterquality.portal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private SampleStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SampleStatus toStatus;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    private String comment;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sample getSample() { return sample; }
    public void setSample(Sample sample) { this.sample = sample; }

    public SampleStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(SampleStatus fromStatus) { this.fromStatus = fromStatus; }

    public SampleStatus getToStatus() { return toStatus; }
    public void setToStatus(SampleStatus toStatus) { this.toStatus = toStatus; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
