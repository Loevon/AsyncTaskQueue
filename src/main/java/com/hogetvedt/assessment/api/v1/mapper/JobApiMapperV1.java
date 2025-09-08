package com.hogetvedt.assessment.api.v1.mapper;

import com.hogetvedt.assessment.api.v1.responses.JobResponseV1;
import com.hogetvedt.assessment.api.v1.responses.JobStatusResponseV1;
import com.hogetvedt.assessment.db.entities.JobEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JobApiMapperV1 {

    public JobResponseV1 mapJobResponse(UUID jobId) {
        return new JobResponseV1(jobId);
    }

    public JobStatusResponseV1 mapJobStatusResponse(JobEntity entity) {
        return new JobStatusResponseV1(entity.getStatus(),
                entity.getAttempts(),
                entity.getLastError(),
                entity.getStartedAt(),
                entity.getCompletedAt());
    }
}
