# Async Task Queue

Job queue in Spring Boot that:
- Accepts jobs via (`POST /v1/jobs`)
- Provides job **status** via (`GET /v1/jobs/{jobId}`)
- Retries with **exponential backoff and jitter**
- Runs them concurrently using a fixed worker pool + **bounded** queue
- When retries are exhausted, runs a **compensation** and marks the job as `COMPENSATED`
- Supports **idempotency** via `idempotencyKey`
- Produces **metrics** and emits **structured logs**


## Run

To run this project first install Docker and Java 24. Then navigate to the project via terminal and run the command:

```bash
docker compose up db
```

Then start the Maven Spring Boot by either running the project in an IDE or run from terminal with:

```bash
mvn spring-boot:run
```

# Testing
To run the projects tests, run this command:
```bash
mvn test
```

# OpenAPI
Once the Spring Boot project loads, you can access the APIs using the following link in your web browser: 

http://localhost:8080/swagger-ui.html

# Metrics
Metrics are logged at these various endpoints here:

- http://127.0.0.1:8080/actuator/metrics/jobs.failed
- http://127.0.0.1:8080/actuator/metrics/jobs.submitted
- http://127.0.0.1:8080/actuator/metrics/jobs.succeeded
- http://127.0.0.1:8080/actuator/metrics/jobs.compensated
- http://127.0.0.1:8080/actuator/metrics/jobs.execution.timer


# Complexity
- Create: O(1) map checks + bounded O(1) enqueue is constant time
- Get: Single database record lookup for a single record
- Delete: Database record deletion

# Assumptions
- No info was given on the jobs that are run, so they are stubs that use delays to simulate work
- If a job fails all of its retries, it will still be marked as compensated - possibly add a "COMPENSATED_FAILED" state?
- 

# What's next?
This project is obviously a demo I put together for an assessment. If I were to continue with it, I would implement some of the following:

- Better database credential storage, such as a secret manager via AWS
- A more robust API result that better supports result data and exceptions
- Add authentication to the APIs as they are all currently insecure
- This is a singleton service. If more instances were spun up, would need to support a locking mechanism for a distributed database
- There is no resume features if the application crashes or is taken down. This means all active tasks would be lost. Need a way to read database rows on server startup that continues execution
