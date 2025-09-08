package com.hogetvedt.assessment.runner;

import com.hogetvedt.assessment.models.types.JobType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobRunnerRegistry {

    private final Map<JobType, JobRunner> runners = new ConcurrentHashMap<>();

    public JobRunnerRegistry() {}

    public JobRunner getJobRunner(JobType jobType) {
        if (runners.containsKey(jobType)) {
            return runners.get(jobType);
        }

        throw new IllegalArgumentException("Job type: " + jobType + " not found.");
    }

    public void register(JobRunner runner) {
        runners.putIfAbsent(runner.getJobType(), runner);
    }
}
