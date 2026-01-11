package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.CreateSnapshotRequest;
import com.codemaniac.jobtrackrai.dto.CreateSnapshotResponse;
import com.codemaniac.jobtrackrai.dto.JobSnapshotDto;
import com.codemaniac.jobtrackrai.service.brightdata.BrightDataService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/snapshots")
public class BrightDataSnapshotController {

  private final BrightDataService brightDataService;

  /** User-facing API: Creates a Bright Data snapshot and returns snapshotId immediately */
  @PostMapping
  public ResponseEntity<CreateSnapshotResponse> createSnapshot(
      @RequestBody final CreateSnapshotRequest request) {

    return ResponseEntity.ok(brightDataService.createSnapshot(request.getJobUrl()));
  }

  /** Internal / admin API: Allows manual snapshot download (useful for retries or debugging) */
  @PostMapping("/{snapshotId}/download")
  public ResponseEntity<List<JobSnapshotDto>> downloadSnapshot(
      @PathVariable final String snapshotId) {

    return ResponseEntity.ok(brightDataService.fetchSnapshotData(snapshotId));
  }
}
