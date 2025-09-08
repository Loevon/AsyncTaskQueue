package com.hogetvedt.assessment.runner.jobs;

import com.hogetvedt.assessment.db.entities.EmailEntity;
import com.hogetvedt.assessment.db.repositories.EmailRepository;
import com.hogetvedt.assessment.models.JobContext;
import com.hogetvedt.assessment.models.states.EmailJobState;
import com.hogetvedt.assessment.models.types.JobType;
import com.hogetvedt.assessment.runner.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

@Component
public class SendEmailJobRunner implements JobRunner<EmailJobState> {

    private static final Logger log = LoggerFactory.getLogger(SendEmailJobRunner.class);

    private final EmailRepository emailRepository;

    @Autowired
    public SendEmailJobRunner(EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @Override
    public JobType getJobType() {
        return JobType.EMAIL;
    }

    @Override
    public void execute(JsonNode payload, JobContext context) throws Exception {
        log.info("Executing EMAIl job -> jobId={}", context.getJobId());

        var entity = new EmailEntity();
        entity.setId(UUID.randomUUID());
        entity.setJobId(context.getJobId());
        entity.setSent(Instant.now());

        // to simulate work being done
        Thread.sleep(1000);
        var state = new EmailJobState(entity.getId());
        context.setLastKnownJobState(state);
        emailRepository.save(entity);

        log.info("Sending email SUCCESS -> jobId={}", context.getJobId());
    }

    @Override
    public void compensate(JobContext context) throws Exception {
        var lastKnownState = (EmailJobState) context.getLastKnownJobState();
        log.info("Compensating failed job -> jobId={}", context.getJobId());
        emailRepository.deleteAllByJobId(lastKnownState.emailId());
    }
}
