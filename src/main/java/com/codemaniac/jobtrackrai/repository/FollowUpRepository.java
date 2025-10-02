package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.FollowUp;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
  List<FollowUp> findByJobApplicationId(Long jobApplicationId);
  List<FollowUp> findByScheduledAtBetween(OffsetDateTime from, OffsetDateTime to);
}

