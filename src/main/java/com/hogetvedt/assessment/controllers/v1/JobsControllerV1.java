package com.hogetvedt.assessment.controllers.v1;

import com.hogetvedt.assessment.api.v1.mapper.JobApiMapperV1;
import com.hogetvedt.assessment.api.v1.requests.JobRequestV1;
import com.hogetvedt.assessment.api.v1.responses.JobResponseV1;
import com.hogetvedt.assessment.api.v1.responses.JobStatusResponseV1;
import com.hogetvedt.assessment.services.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/jobs")
public class JobsControllerV1 {

    private final JobApiMapperV1 apiMapper;
    private final JobQueueService jobQueueService;

    @Autowired
    public JobsControllerV1(JobQueueService jobQueueService, JobApiMapperV1 apiMapper) {
        this.apiMapper = apiMapper;
        this.jobQueueService = jobQueueService;
    }

    @GetMapping("/{jobId}")
    public JobStatusResponseV1 getJobs(@PathVariable UUID jobId) {
        var result = jobQueueService.getJob(jobId);
        return apiMapper.mapJobStatusResponse(result);
    }

    @PostMapping
    public JobResponseV1 createJob(@RequestBody JobRequestV1 request) {
        var id = jobQueueService.createJob(request.type(), request.payload(), request.idempotencyKey());
        return apiMapper.mapJobResponse(id);
    }

    @DeleteMapping("/{jobId}")
    public void deleteJobRecord(@PathVariable UUID jobId) {
        jobQueueService.deleteJob(jobId);
    }
}
