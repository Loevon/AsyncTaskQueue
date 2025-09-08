package com.hogetvedt.assessment.api.v1.responses;

import com.hogetvedt.assessment.models.types.JobStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record JobStatusResponseV1(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "QUEUED|RUNNING|SUCCEEDED|FAILED|COMPENSATED") JobStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2") int attempts,
        @Schema(example = "SMTP 451 Temporary local problem") String lastError,
        @Schema(example = "2025-09-07T20:30:00Z") Instant startedAt,
        @Schema(example = "2025-09-07T20:31:30Z") Instant completedAt
) {}
