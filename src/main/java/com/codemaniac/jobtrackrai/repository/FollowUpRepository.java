package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.FollowUp;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
  List<FollowUp> findByJobApplicationId(Long jobApplicationId);

  List<FollowUp> findByScheduledAtBetween(OffsetDateTime from, OffsetDateTime to);
}
