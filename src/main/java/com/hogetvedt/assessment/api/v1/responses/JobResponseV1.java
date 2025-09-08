package com.hogetvedt.assessment.api.v1.responses;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record JobResponseV1(
        @Schema(description = "Server-assigned Job ID", requiredMode = Schema.RequiredMode.REQUIRED) UUID jobId
) {}
