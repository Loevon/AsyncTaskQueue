package com.hogetvedt.assessment.runner;

import com.hogetvedt.assessment.models.JobContext;
import com.hogetvedt.assessment.models.states.JobState;
import com.hogetvedt.assessment.models.types.JobType;
import tools.jackson.databind.JsonNode;

public interface JobRunner <T extends JobState> {

    JobType getJobType();

    void execute(JsonNode payload, JobContext context) throws Exception;

    void compensate(JobContext context) throws Exception;
}
