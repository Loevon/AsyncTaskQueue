package com.hogetvedt.assessment;

import com.hogetvedt.assessment.api.v1.requests.JobRequestV1;
import com.hogetvedt.assessment.api.v1.responses.JobResponseV1;
import com.hogetvedt.assessment.api.v1.responses.JobStatusResponseV1;
import com.hogetvedt.assessment.models.JobRunnerProperties;
import com.hogetvedt.assessment.models.types.JobStatus;
import com.hogetvedt.assessment.models.types.JobType;
import com.hogetvedt.assessment.runner.jobs.TestJobRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobQueueIntegrationTests {

	private final MockMvc mvc;
	private final ObjectMapper mapper;
	private final TestJobRunner testJobRunner;
	private final JobRunnerProperties properties;

	@Autowired
	public JobQueueIntegrationTests(MockMvc mvc, TestJobRunner testJobRunner, JobRunnerProperties properties, ObjectMapper mapper) {
		this.mvc = mvc;
        this.mapper = mapper;
		this.properties = properties;
		this.testJobRunner = testJobRunner;
	}

	@Test
	void compensationTest() throws Exception {
		var idempotenceKey = UUID.randomUUID().toString();
		var payload = mapper.valueToTree(Map.of("test", "compensation" + idempotenceKey));
		var request = new JobRequestV1(JobType.TEST, payload, idempotenceKey);
		var body = mapper.writeValueAsString(request);
		var response = mvc.perform(post("/v1/jobs")
				.contentType("application/json").content(body))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		// give time to let the job fail
		Thread.sleep(15000); // TODO: This should be some kind of a callback that says "the job has finished" instead of a timer

		var jobId = mapper.readValue(response, JobResponseV1.class).jobId();
		var jobStatus = mvc.perform(get("/v1/jobs/" + jobId))
						.andExpect(status().isOk())
								.andReturn().getResponse().getContentAsString();
		var jobStatusResponse = mapper.readValue(jobStatus, JobStatusResponseV1.class);

		assertThat(JobStatus.valueOf(jobStatusResponse.status().toString()).equals(JobStatus.COMPENSATED));
		assertThat(jobStatusResponse.lastError() != null);
		assertThat(testJobRunner.hasRecord(jobId)).isFalse();	// simulating a database call

		// test cleanup
		mvc.perform(delete("/v1/jobs/" + jobId));
	}

	@Test
	void idempotencyTest() throws Exception {
		var idempotenceKey = UUID.randomUUID().toString();
		var payload = mapper.valueToTree(Map.of("test", "email" + idempotenceKey));
		var request = new JobRequestV1(JobType.REPORT, payload, idempotenceKey);
		var body = mapper.writeValueAsString(request);
		var response1 = mvc.perform(post("/v1/jobs")
						.contentType("application/json").content(body))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		var response2 = mvc.perform(post("/v1/jobs")
						.contentType("application/json").content(body))
				.andExpect(status().is2xxSuccessful())
				.andReturn().getResponse().getContentAsString();

		var jobResponse1 = mapper.readValue(response1, JobResponseV1.class);
		var jobResponse2 = mapper.readValue(response2, JobResponseV1.class);

		assertThat(jobResponse1.jobId()).isEqualTo(jobResponse2.jobId());

		var jobStatus = mvc.perform(get("/v1/jobs/" + jobResponse1.jobId()))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		var jobStatusResponse = mapper.readValue(jobStatus, JobStatusResponseV1.class);

		assertThat(JobStatus.valueOf(jobStatusResponse.status().toString()).equals(JobStatus.SUCCEEDED));

		// test cleanup
		mvc.perform(delete("/v1/jobs/" + jobResponse2.jobId()));
	}

	@Test
	void fullQueueTest() throws Exception {
		var jobIds = new HashSet<UUID>();
		var payload = mapper.valueToTree(Map.of("test", "document"));
		var request = new JobRequestV1(JobType.REPORT, payload, null);
		var body = mapper.writeValueAsString(request);
		var maxTestCount = properties.queueCapacity() + properties.maxPoolSize();

		for (int i = 0; i < maxTestCount; i++) {
			var response = mvc.perform(post("/v1/jobs")
							.contentType("application/json").content(body))
					.andExpect(status().is2xxSuccessful())
					.andReturn().getResponse().getContentAsString();
			var jobId = mapper.readValue(response, JobResponseV1.class).jobId();
			jobIds.add(jobId);
		}

		// make call that exceeds capacity
		var responseStatus = mvc.perform(post("/v1/jobs").contentType("application/json").content(body))
				.andReturn()
				.getResponse()
				.getStatus();

		assertThat(responseStatus == 429).isTrue();

		// cleanup
		jobIds.forEach(jobId -> {
            try {
                mvc.perform(delete("/v1/jobs/" + jobId));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
	}
}
