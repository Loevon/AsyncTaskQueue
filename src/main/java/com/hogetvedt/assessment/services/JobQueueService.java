package com.hogetvedt.assessment.services;

import com.hogetvedt.assessment.db.entities.JobEntity;
import com.hogetvedt.assessment.db.repositories.JobRepository;
import com.hogetvedt.assessment.exceptions.QueueFullException;
import com.hogetvedt.assessment.exceptions.ResourceNotFoundException;
import com.hogetvedt.assessment.metrics.JobMetrics;
import com.hogetvedt.assessment.models.JobContext;
import com.hogetvedt.assessment.models.JobRunnerProperties;
import com.hogetvedt.assessment.models.types.JobStatus;
import com.hogetvedt.assessment.models.types.JobType;
import com.hogetvedt.assessment.runner.JobRunnerRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class JobQueueService {

    private static final Logger log = LoggerFactory.getLogger(JobQueueService.class);

    private final JobMetrics metrics;
    private final ObjectMapper objectMapper;
    private final JobRunnerRegistry registry;
    private final JobRepository jobRepository;
    private final ThreadPoolExecutor executor;
    private final JobRunnerProperties properties;
    private final ScheduledExecutorService scheduler;

    public JobQueueService(JobRunnerRegistry registry, JobRepository jobRepository, ThreadPoolExecutor executor, JobRunnerProperties properties,
                           ScheduledExecutorService scheduler, JobMetrics metrics, ObjectMapper objectMapper) {
        this.metrics = metrics;
        this.registry = registry;
        this.executor = executor;
        this.scheduler = scheduler;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
    }

    public JobEntity getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with ID: %s not found".formatted(jobId)));
    }

    public UUID createJob(JobType type, JsonNode payload, String idempotencyKey) {
        if (idempotencyKey != null) {
            var existingJob = jobRepository.findByIdempotencyKey(idempotencyKey);

            if (existingJob.isPresent()) {
                return existingJob.get().getId();
            }
        }

        var entity = createEntity();
        entity.setType(type);
        entity.setPayload(payload.toString());
        entity.setIdempotencyKey(idempotencyKey);
        entity.setMaxRetries(properties.maxRetries());

        log.info("Created new job -> jobId={} idempotencyKey={}", entity.getId(), entity.getIdempotencyKey());

        try {
            jobRepository.saveAndFlush(entity);
            executor.execute(() -> runJob(entity));
            metrics.getJobsSubmitted().increment();
            return entity.getId();
        } catch (RejectedExecutionException ree) {
            log.error("Exception occurred when running job -> jobId={} error={}", entity.getId(), ree.getMessage());
            jobRepository.deleteById(entity.getId());
            throw ree;
        } catch (QueueFullException e) {
            log.warn("Job queue capacity reached.");
            jobRepository.deleteById(entity.getId());
            throw e;
        }
    }

    void runJob(JobEntity entity) {
        int attempts = entity.getAttempts() + 1;
        var runner = registry.getJobRunner(entity.getType());

        entity.setAttempts(attempts);
        entity.setStatus(JobStatus.RUNNING);

        if (entity.getStartedAt() == null) {
            entity.setStartedAt(Instant.now());
        }

        jobRepository.saveAndFlush(entity);

        var context = new JobContext(entity.getId(), entity.getIdempotencyKey(), attempts);
        Timer.Sample timer = Timer.start();

        try {
            var payload = objectMapper.readTree(entity.getPayload());
            runner.execute(payload, context);
            entity.setStatus(JobStatus.SUCCEEDED);
            metrics.getJobsSucceeded().increment();
        } catch (Exception e) {
            entity.setStatus(JobStatus.FAILED);
            entity.setLastError(e.getMessage());
            metrics.getJobsFailed().increment();

            log.warn("Job failed -> jobId={} type={} attempt={} error={}", entity.getId(), entity.getType(), attempts, e.getMessage());

            var delay = getAttemptDelay(attempts);

            if (attempts <= entity.getMaxRetries()) {
                log.info("Job retry -> jobId={} attempt={} delay={}", entity.getId(), attempts, delay);

                scheduler.schedule(() -> {
                    try {
                        var newDuration = entity.getDuration() + ((double) delay / 1000);
                        entity.setDuration(newDuration);
                        executor.execute(() -> runJob(entity));
                    } catch (RejectedExecutionException ree) {
                        var newDuration = entity.getDuration() + ((double) delay / 50);
                        entity.setDuration(newDuration);
                        scheduler.schedule(() -> executor.execute(() -> runJob(entity)), 50, TimeUnit.MILLISECONDS);
                    }
                }, delay, TimeUnit.MILLISECONDS);

                entity.setStatus(JobStatus.QUEUED);
            } else {
                entity.setStatus(JobStatus.COMPENSATED);
                log.error("Job has reached maximum number of retries -> jobId={}", entity.getId());

                try {
                    runner.compensate(context);
                    metrics.getJobsCompensated().increment();
                    log.info("Job has been compensated -> jobId={}", entity.getId());
                } catch (Exception ex) {
                    log.error("Job was unable to be compensated -> jobId={} error={}", entity.getId(), ex.toString());
                }
            }
        } finally {
            var timerResult = timer.stop(metrics.getJobTimer());
            var currTime = Instant.now();
            var totalTime = currTime.toEpochMilli() - entity.getStartedAt().toEpochMilli();

            entity.setCompletedAt(currTime);
            entity.setDuration(totalTime);
            jobRepository.save(entity);

            log.info("Job attempt has finished -> jobId={} duration={} status={}", entity.getId(), timerResult, entity.getStatus());
        }
    }

    public void deleteJob(UUID jobId) {
        jobRepository.deleteById(jobId);
    }

    private JobEntity createEntity() {
        var id =  UUID.randomUUID();
        var entity = new JobEntity();
        entity.setId(id);
        entity.setStatus(JobStatus.QUEUED);
        return entity;
    }

    // get simple exponential delay with random jitter
    private long getAttemptDelay(int attempt) {
        var jitter = ThreadLocalRandom.current().nextLong(0, properties.jitterDelay());
        return (long) Math.pow(attempt, attempt - 1) * jitter;
    }
}
