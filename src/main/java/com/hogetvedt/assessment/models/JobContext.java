package com.hogetvedt.assessment.models;

import com.hogetvedt.assessment.models.states.JobState;

import java.util.UUID;

public class JobContext {

    private final UUID jobId;
    private final String idempotencyKey;

    private int attempts;
    private double duration;
    private JobState lastKnownJobState;

    public JobContext(UUID jobId, String idempotencyKey, int attempts) {
        this.jobId = jobId;
        this.duration = 0L;
        this.attempts = attempts;
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getJobId() { return jobId; }
    public String getIdempotencyKey() { return idempotencyKey; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public double getDuration() { return duration; }
    public void setDuration(double duration) { this.duration = duration; }

    public JobState getLastKnownJobState() { return lastKnownJobState; }
    public void setLastKnownJobState(JobState lastKnownJobState) { this.lastKnownJobState = lastKnownJobState; }
}
