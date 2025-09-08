package com.hogetvedt.assessment.api.v1.requests;

import com.hogetvedt.assessment.models.types.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.JsonNode;

@Schema(name = "JobRequestV1", description = "Payload to submit a job to the queue")
public record JobRequestV1(
        @Schema(description = "Type of job to execute", requiredMode = Schema.RequiredMode.REQUIRED, example = "EMAIL")
        JobType type,

        @Schema(description = "Arbitrary JSON payload for the job",
                requiredMode = Schema.RequiredMode.REQUIRED,
                type = "object",
                example = """
            { "to":"matthew@example.com", "subject":"Welcome", "body":"Hello!" }
            """)
        JsonNode payload,

        @Schema(description = "Optional idempotency key to prevent duplicates", example = "email-2025-09-06-1234")
        String idempotencyKey) {}
