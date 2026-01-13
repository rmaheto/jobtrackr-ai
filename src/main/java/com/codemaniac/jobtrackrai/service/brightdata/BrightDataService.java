package com.codemaniac.jobtrackrai.service.brightdata;

import com.codemaniac.jobtrackrai.dto.CreateSnapshotResponse;
import com.codemaniac.jobtrackrai.dto.JobSnapshotDto;
import com.codemaniac.jobtrackrai.mapper.IndeedJobSnapshotMapper;
import com.codemaniac.jobtrackrai.util.IndeedJobUrlValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrightDataService {

  private final BrightDataClient brightDataClient;

  public CreateSnapshotResponse createSnapshot(final String jobUrl) {

    if (!IndeedJobUrlValidator.isValid(jobUrl)) {
      throw new IllegalArgumentException("Invalid Indeed job URL");
    }

    final String snapshotId = brightDataClient.requestSnapshotId(jobUrl);

    return new CreateSnapshotResponse(snapshotId);
  }

  public List<JobSnapshotDto> fetchSnapshotData(final String snapshotId) {

    return brightDataClient.fetchSnapshotData(snapshotId).stream()
        .map(IndeedJobSnapshotMapper::toJobSnapshotDto)
        .toList();
  }
}
