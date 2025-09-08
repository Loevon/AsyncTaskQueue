package com.hogetvedt.assessment.db.repositories;

import com.hogetvedt.assessment.db.entities.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, UUID>{

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from EmailEntity e where e.jobId = :jobId")
    void deleteAllByJobId(@Param("jobId") UUID jobId);
}
