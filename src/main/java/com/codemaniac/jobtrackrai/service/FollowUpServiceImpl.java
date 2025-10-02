package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.FollowUpDto;
import com.codemaniac.jobtrackrai.dto.FollowUpRequest;
import com.codemaniac.jobtrackrai.entity.FollowUp;
import com.codemaniac.jobtrackrai.entity.JobApplication;
import com.codemaniac.jobtrackrai.exception.NotFoundException;
import com.codemaniac.jobtrackrai.mapper.FollowUpMapper;
import com.codemaniac.jobtrackrai.repository.FollowUpRepository;
import com.codemaniac.jobtrackrai.repository.JobApplicationRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowUpServiceImpl implements FollowUpService {

  private final FollowUpRepository followUpRepository;
  private final JobApplicationRepository jobApplicationRepository;
  private final FollowUpMapper followUpMapper;

  @Transactional
  public FollowUpDto scheduleFollowUp(final Long applicationId, final FollowUpRequest request) {
    final JobApplication jobApplication =
        jobApplicationRepository
            .findById(applicationId)
            .orElseThrow(() -> new NotFoundException(applicationId));

    final FollowUp followUp =
        FollowUp.builder()
            .scheduledAt(request.getScheduledAt())
            .type(request.getType())
            .notes(request.getNotes())
            .jobApplication(jobApplication)
            .completed(false)
            .build();

    final FollowUp saved = followUpRepository.save(followUp);
    return followUpMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  public List<FollowUpDto> getFollowUps(final Long applicationId) {
    return followUpRepository.findByJobApplicationId(applicationId).stream()
        .map(followUpMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FollowUpDto> findFollowUpsBetween(final OffsetDateTime from, final OffsetDateTime to) {
    return followUpRepository.findByScheduledAtBetween(from, to)
        .stream()
        .map(followUpMapper::toDto)
        .toList();
  }


  @Transactional
  public FollowUpDto markCompleted(final Long followUpId) {
    final FollowUp followUp =
        followUpRepository
            .findById(followUpId)
            .orElseThrow(() -> new NotFoundException(followUpId));
    followUp.setCompleted(true);
    return followUpMapper.toDto(followUpRepository.save(followUp));
  }

  @Transactional
  public void deleteFollowUp(final Long followUpId) {
    if (!followUpRepository.existsById(followUpId)) {
      throw new NotFoundException(followUpId);
    }
    followUpRepository.deleteById(followUpId);
  }
}
