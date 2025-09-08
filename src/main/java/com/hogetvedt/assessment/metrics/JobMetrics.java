package com.hogetvedt.assessment.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class JobMetrics {

    private final Timer jobTimer;
    private final Counter jobsFailed;
    private final Counter jobsSubmitted;
    private final Counter jobsSucceeded;
    private final Counter jobsCompensated;

    public JobMetrics(MeterRegistry registry) {
        this.jobTimer = Timer.builder("jobs.execution.timer")
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(registry);

        jobsFailed = Counter.builder("jobs.failed").register(registry);
        jobsSubmitted = Counter.builder("jobs.submitted").register(registry);
        jobsSucceeded = Counter.builder("jobs.succeeded").register(registry);
        jobsCompensated = Counter.builder("jobs.compensated").register(registry);
    }

    public Timer getJobTimer() { return jobTimer; }
    public Counter getJobsFailed() { return jobsFailed; }
    public Counter getJobsSubmitted() { return jobsSubmitted; }
    public Counter getJobsSucceeded() { return jobsSucceeded; }
    public Counter getJobsCompensated() { return jobsCompensated; }
}
