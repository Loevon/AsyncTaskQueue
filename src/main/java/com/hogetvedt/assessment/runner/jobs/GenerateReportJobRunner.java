package com.hogetvedt.assessment.runner.jobs;

import com.hogetvedt.assessment.models.JobContext;
import com.hogetvedt.assessment.models.states.GenerateReportJobState;
import com.hogetvedt.assessment.models.types.JobType;
import com.hogetvedt.assessment.runner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
public class GenerateReportJobRunner implements JobRunner<GenerateReportJobState> {

    private static final Logger log = LoggerFactory.getLogger(GenerateReportJobRunner.class);

    @Override
    public JobType getJobType() {
        return JobType.REPORT;
    }

    @Override
    public void execute(JsonNode payload, JobContext context) throws Exception {
        log.info("Executing REPORT job -> jobId={}", context.getJobId());
        var state = new GenerateReportJobState();
        context.setLastKnownJobState(state);
        // simulate work delay
        Thread.sleep(2000);

        log.info("Generating report SUCCESS -> jobId={}", context.getJobId());
    }

    @Override
    public void compensate(JobContext context) throws Exception {}
}
