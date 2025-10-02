package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.dto.JobApplicationSearchRequest;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.model.Audit;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public class JobApplicationSpecifications {

  private static final String APPLIED_DATE = "appliedDate";

  private JobApplicationSpecifications() {}

  public static Specification<JobApplication> forSearch(
      final JobApplicationSearchRequest request, final Long userId) {
    return (root, query, cb) -> {
      final List<Predicate> predicates = new ArrayList<>();

      predicates.add(cb.equal(root.get("user").get("id"), userId));
      predicates.add(cb.equal(root.get("audit").get("recordStatus"), Audit.RECORD_STATUS_ACTIVE));

      addLikeIfPresent(cb, root, predicates, "company", request.getCompany());
      addLikeIfPresent(cb, root, predicates, "role", request.getRole());
      addEqualIfPresent(cb, root, predicates, "location", request.getLocation());
      addEqualIfPresent(cb, root, predicates, "jobType", request.getJobType());
      addLikeIfPresent(cb, root, predicates, "skills", request.getSkills());

      Optional.ofNullable(request.getStatus())
          .ifPresent(
              status -> predicates.add(cb.equal(root.get("status"), Status.valueOf(status))));

      addDateRange(cb, root, predicates, request);

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private static void addLikeIfPresent(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final String field,
      final String val) {
    Optional.ofNullable(val)
        .map(v -> cb.like(cb.lower(root.get(field)), "%" + v.toLowerCase() + "%"))
        .ifPresent(predicateList::add);
  }

  private static void addEqualIfPresent(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final String field,
      final Object val) {
    Optional.ofNullable(val).map(v -> cb.equal(root.get(field), v)).ifPresent(predicateList::add);
  }

  private static void addDateRange(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final JobApplicationSearchRequest req) {
    if (req.getFromDate() != null && req.getToDate() != null) {
      predicateList.add(cb.between(root.get(APPLIED_DATE), req.getFromDate(), req.getToDate()));
    } else if (req.getFromDate() != null) {
      predicateList.add(cb.greaterThanOrEqualTo(root.get(APPLIED_DATE), req.getFromDate()));
    } else if (req.getToDate() != null) {
      predicateList.add(cb.lessThanOrEqualTo(root.get(APPLIED_DATE), req.getToDate()));
    }
  }
}
