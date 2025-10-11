package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.dto.JobApplicationSummaryDto;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository
    extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

  List<JobApplication> findAllByUserIdAndAudit_RecordStatus(Long id, String status);

  List<JobApplication> findByUserIdAndAudit_RecordStatusOrderByAudit_CreateTimestampDesc(
      Long id, String status);

  @Query(
      "SELECT new com.codemaniac.jobtrackrai.dto.JobApplicationSummaryDto("
          + "a.id, a.company, a.role, a.status, a.appliedDate) "
          + "FROM JobApplication a "
          + "WHERE a.user.id = :userId AND a.audit.recordStatus = :recordStatus")
  List<JobApplicationSummaryDto> findLiteByUserIdAndStatus(
      @Param("userId") Long userId, @Param("recordStatus") String recordStatus);
}
