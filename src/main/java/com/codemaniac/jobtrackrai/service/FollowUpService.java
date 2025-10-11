package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.FollowUpDto;
import com.codemaniac.jobtrackrai.dto.FollowUpRequest;
import java.time.OffsetDateTime;
import java.util.List;

public interface FollowUpService {
  FollowUpDto scheduleFollowUp(Long applicationId, FollowUpRequest request);

  List<FollowUpDto> getFollowUps(Long applicationId);

  public List<FollowUpDto> findFollowUpsBetween(final OffsetDateTime from, final OffsetDateTime to);

  FollowUpDto markCompleted(Long followUpId);

  void deleteFollowUp(Long followUpId);
}
