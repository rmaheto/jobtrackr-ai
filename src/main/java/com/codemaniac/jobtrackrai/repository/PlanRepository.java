package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.Plan;
import com.codemaniac.jobtrackrai.enums.PlanCode;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlanRepository extends JpaRepository<Plan, Long> {

  Optional<Plan> findByCode(PlanCode code);

  Optional<Plan> findByCodeAndActiveTrue(String code);

  Optional<Plan> findByStripePriceId(String stripePriceId);

  List<Plan> findAllByActiveTrue();

  @Query(
      """
  select p
  from Plan p
  where p.stripePriceId = :stripePriceId
    and p.active = true
""")
  Optional<Plan> findActiveByStripePriceId(@Param("stripePriceId") String stripePriceId);
}
