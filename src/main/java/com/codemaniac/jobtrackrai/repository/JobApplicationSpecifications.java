package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.dto.JobApplicationSearchRequest;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.enums.Status;
import com.codemaniac.jobtrackrai.model.Audit;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
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

      // Always filter by userId and active records
      predicates.add(cb.equal(root.get("user").get("id"), userId));
      predicates.add(cb.equal(root.get("audit").get("recordStatus"), Audit.RECORD_STATUS_ACTIVE));

      // Global searchTerm across multiple fields
      addSearchTerm(cb, root, predicates, request.getSearchTerm());

      // Individual field filters
      addLikeIfPresent(cb, root, predicates, "company", request.getCompany());
      addLikeIfPresent(cb, root, predicates, "role", request.getRole());
      addLikeIfPresent(cb, root, predicates, "skills", request.getSkills());

      addEqualIfPresent(cb, root, predicates, "location", request.getLocation());
      addEqualIfPresent(cb, root, predicates, "jobType", request.getJobType());

      Optional.ofNullable(request.getStatus())
          .map(String::toUpperCase)
          .ifPresent(
              status -> predicates.add(cb.equal(root.get("status"), Status.valueOf(status))));

      // Date filters
      addDateRange(cb, root, predicates, request.getFromDate(), request.getToDate());

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private static void addSearchTerm(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final String searchTerm) {
    Optional.ofNullable(searchTerm)
        .filter(term -> !term.isBlank())
        .ifPresent(
            term -> {
              final String likePattern = "%" + term.toLowerCase() + "%";
              final Predicate companyLike = cb.like(cb.lower(root.get("company")), likePattern);
              final Predicate roleLike = cb.like(cb.lower(root.get("role")), likePattern);
              final Predicate locationLike = cb.like(cb.lower(root.get("location")), likePattern);
              final Predicate skillsLike = cb.like(cb.lower(root.get("skills")), likePattern);

              // OR across multiple fields
              predicateList.add(cb.or(companyLike, roleLike, locationLike, skillsLike));
            });
  }

  private static void addLikeIfPresent(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final String field,
      final String val) {
    Optional.ofNullable(val)
        .filter(v -> !v.isBlank())
        .map(v -> cb.like(cb.lower(root.get(field)), "%" + v.toLowerCase() + "%"))
        .ifPresent(predicateList::add);
  }

  private static void addEqualIfPresent(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final String field,
      final Object val) {
    Optional.ofNullable(val).ifPresent(v -> predicateList.add(cb.equal(root.get(field), v)));
  }

  private static void addDateRange(
      final CriteriaBuilder cb,
      final Root<JobApplication> root,
      final List<Predicate> predicateList,
      final LocalDate fromDate,
      final LocalDate toDate) {

    if (fromDate != null && toDate != null) {
      predicateList.add(cb.between(root.get(APPLIED_DATE), fromDate, toDate));
    } else if (fromDate != null) {
      predicateList.add(cb.greaterThanOrEqualTo(root.get(APPLIED_DATE), fromDate));
    } else if (toDate != null) {
      predicateList.add(cb.lessThanOrEqualTo(root.get(APPLIED_DATE), toDate));
    }
  }
}
