package com.hogetvedt.assessment.config;

import com.hogetvedt.assessment.exceptions.QueueFullException;
import com.hogetvedt.assessment.models.JobRunnerProperties;
import com.hogetvedt.assessment.runner.JobRunner;
import com.hogetvedt.assessment.runner.JobRunnerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.concurrent.*;

@EnableAsync
@Configuration
public class SpringConfig {

    @Bean
    public ThreadPoolExecutor jobExecutor(JobRunnerProperties properties) {
        var queue = new ArrayBlockingQueue<Runnable>(properties.queueCapacity(), true);

        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("worker-" + thread.getName());
            thread.setDaemon(true);
            return thread;
        };

        RejectedExecutionHandler reject =
                (runnable, executor) -> {
                    int size = executor.getQueue().size();
                    int cap  = size + executor.getQueue().remainingCapacity();
                    throw new QueueFullException("Job queue is full (" + size + "/" + cap + ")");
                };

        return new ThreadPoolExecutor(properties.corePoolSize(), properties.maxPoolSize(), 0L, TimeUnit.MILLISECONDS, queue, threadFactory, reject);
    }

    @Bean
    public ScheduledExecutorService scheduler() {
        return Executors.newScheduledThreadPool(1, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("job-scheduler-" + thread.getName());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Bean
    public JobRunnerRegistry jobRunnerRegistry(Collection<JobRunner> runners) {
        var registry = new JobRunnerRegistry();
        runners.forEach(registry::register);
        return registry;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
