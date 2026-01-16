package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.Plan;
import com.codemaniac.jobtrackrai.enums.PlanCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {

  Optional<Plan> findByCode(PlanCode code);

  Optional<Plan> findByCodeAndActiveTrue(PlanCode code);

  Optional<Plan> findByStripePriceId(String stripePriceId);

  List<Plan> findAllByActiveTrue();
}
