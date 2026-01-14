package com.codemaniac.jobtrackrai.service.brightdata;

import com.codemaniac.jobtrackrai.enums.BrightDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndeedSnapshotService {

  @Value("${brightdata.indeed.dataset-id}")
  private String datasetId;

  private final BrightDataClient brightDataClient;

  public String requestSnapshot(final String jobUrl) {
    return brightDataClient.requestSnapshotId(datasetId, jobUrl, BrightDataSource.INDEED);
  }
}
