package com.hogetvedt.assessment.models;

import com.hogetvedt.assessment.db.entities.JobEntity;
import com.hogetvedt.assessment.models.types.JobType;

import java.util.UUID;

public class Job {

    private UUID id;
    private JobType type;
    private String idempotencyKey;

    public Job(JobEntity entity) {

    }
}
