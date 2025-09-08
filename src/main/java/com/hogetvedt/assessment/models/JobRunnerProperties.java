package com.hogetvedt.assessment.models;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jobs.executor")
public record JobRunnerProperties(
        @Min(1)
        int corePoolSize,

        @Min(1)
        int maxPoolSize,

        @Min(0)
        int queueCapacity,

        @Min(0)
        @Max(10000)
        int jitterDelay,

        @Min(0)
        int maxRetries
) {

    public JobRunnerProperties {
        if (maxPoolSize < corePoolSize) {
            throw new IllegalArgumentException("maxPoolSize must be >= corePoolSize");
        }
    }
}
