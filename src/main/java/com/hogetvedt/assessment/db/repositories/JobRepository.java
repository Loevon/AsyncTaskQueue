package com.hogetvedt.assessment.db.repositories;

import com.hogetvedt.assessment.db.entities.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    Optional<JobEntity> findByIdempotencyKey(String idempotencyKey);
}
