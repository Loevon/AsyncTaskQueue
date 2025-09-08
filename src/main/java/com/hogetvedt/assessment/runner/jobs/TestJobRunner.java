package com.hogetvedt.assessment.runner.jobs;

import com.hogetvedt.assessment.models.JobContext;
import com.hogetvedt.assessment.models.states.TestJobState;
import com.hogetvedt.assessment.models.types.JobType;
import com.hogetvedt.assessment.runner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TestJobRunner implements JobRunner<TestJobState> {

    private static final Logger log = LoggerFactory.getLogger(TestJobRunner.class);

    private final Set<UUID> records = ConcurrentHashMap.newKeySet();

    public TestJobRunner() {}

    @Override
    public JobType getJobType() {
        return JobType.TEST;
    }

    @Override
    public void execute(JsonNode payload, JobContext context) throws Exception {
        log.info("Executing TEST job -> jobId={}", context.getJobId());
        var state = new TestJobState(context.getJobId());
        context.setLastKnownJobState(state);
        records.add(context.getJobId());
        throw new RuntimeException("Exception occurred while executing test job -> jobId=" + context.getJobId());
    }

    @Override
    public void compensate(JobContext context) throws Exception {
        var lastKnownState = (TestJobState) context.getLastKnownJobState();

        if (hasRecord(lastKnownState.jobId())) {
            records.remove(context.getJobId());
        }
    }

    public boolean hasRecord(UUID jobId) {
        return records.contains(jobId);
    }
}
