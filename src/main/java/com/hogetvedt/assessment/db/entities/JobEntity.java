package com.hogetvedt.assessment.db.entities;

import com.hogetvedt.assessment.models.types.JobStatus;
import com.hogetvedt.assessment.models.types.JobType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "jobs")
public class JobEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false)
    private int maxRetries;

    @Column
    private double duration;

    @Column
    private String lastError;

    @Column
    private Instant startedAt;

    @Column
    private Instant completedAt;

    @Column
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR")
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR")
    private JobStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;

    public JobEntity() {
        this.maxRetries = 0;
        this.attempts = 0;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public JobType getType() { return type; }
    public void setType(JobType type) { this.type = type; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
