package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.JobApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository
    extends JpaRepository<JobApplication, Long>, JpaSpecificationExecutor<JobApplication> {

  List<JobApplication> findAllByUserIdAndAudit_RecordStatus(Long id, String status);

  List<JobApplication> findByUserIdAndAudit_RecordStatusOrderByAudit_CreateTimestampDesc(
      Long id, String status);

  List<JobApplication> findLiteByUserIdAndAudit_RecordStatus(Long userId, String recordStatus);

  Optional<JobApplication> findBySnapshotId(String snapshotId);

  Optional<JobApplication> findByJobLink(String jobLink);

  long countByUserIdAndAudit_RecordStatus(Long id, String status);
}
