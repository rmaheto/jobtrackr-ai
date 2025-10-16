package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.FollowUpDto;
import com.codemaniac.jobtrackrai.dto.FollowUpRequest;
import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.FollowUpService;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FollowUpController {

  private final FollowUpService followUpService;

  @PostMapping("/applications/{applicationId}/followups")
  public ResponseEntity<ApiResponse<FollowUpDto>> scheduleFollowUp(
      @PathVariable final Long applicationId, @RequestBody final FollowUpRequest request) {

    final FollowUpDto dto = followUpService.scheduleFollowUp(applicationId, request);
    return ResponseEntity.ok(ApiResponse.of("OK", "Follow-up scheduled", dto));
  }

  @GetMapping("/applications/{applicationId}/followups")
  public ResponseEntity<ApiResponse<List<FollowUpDto>>> getFollowUpsByApplication(
      @PathVariable final Long applicationId) {
    final List<FollowUpDto> followUps = followUpService.getFollowUps(applicationId);
    return ResponseEntity.ok(ApiResponse.of("OK", "Follow-ups retrieved", followUps));
  }

  @GetMapping("/followups")
  public ResponseEntity<ApiResponse<List<FollowUpDto>>> getFollowUpsInRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to) {
    return ResponseEntity.ok(
        ApiResponse.of(
            "OK", "Follow-ups retrieved", followUpService.findFollowUpsBetween(from, to)));
  }

  @PutMapping("/followups/{followUpId}")
  public ResponseEntity<ApiResponse<FollowUpDto>> updateFollowUp(
      @PathVariable final Long followUpId, @RequestBody final FollowUpRequest request) {

    final FollowUpDto dto = followUpService.updateFollowUp(followUpId, request);
    return ResponseEntity.ok(ApiResponse.of("OK", "Follow-up updated", dto));
  }


  @PatchMapping("/followups/{followUpId}/complete")
  public ResponseEntity<ApiResponse<FollowUpDto>> markCompleted(
      @PathVariable final Long followUpId) {
    final FollowUpDto dto = followUpService.markCompleted(followUpId);
    return ResponseEntity.ok(ApiResponse.of("OK", "Follow-up marked completed", dto));
  }

  @DeleteMapping("/followups/{followUpId}")
  public ResponseEntity<ApiResponse<Void>> deleteFollowUp(@PathVariable final Long followUpId) {
    followUpService.deleteFollowUp(followUpId);
    return ResponseEntity.ok(ApiResponse.of("OK", "Follow-up deleted"));
  }
}
