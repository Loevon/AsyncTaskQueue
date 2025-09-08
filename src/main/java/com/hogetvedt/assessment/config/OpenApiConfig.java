package com.hogetvedt.assessment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI assessmentApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Assessment Job Queue API")
                        .version("v1")
                        .description("""
                    Async Task Queue with retries and compensation.
                    Endpoints to submit jobs, query status, and manage records.
                    """)
                        .contact(new Contact().name("Platform Team").email("platform@example.com"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")));
    }
}