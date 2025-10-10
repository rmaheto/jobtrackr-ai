package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.ResumeDto;
import com.codemaniac.jobtrackrai.model.ApiResponse;
import com.codemaniac.jobtrackrai.service.ResumeService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

  private final ResumeService resumeService;

  @PostMapping(consumes = {"multipart/form-data"})
  public ResponseEntity<ApiResponse<ResumeDto>> uploadResume(
      @RequestPart("file") final MultipartFile file) {

    final ResumeDto dto = resumeService.uploadResume(file);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.of(HttpStatus.CREATED.name(), "Resume uploaded successfully", dto));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<ResumeDto>>> listResumes() {
    final List<ResumeDto> resumes = resumeService.listResumes();
    return ResponseEntity.ok(
        ApiResponse.of(HttpStatus.OK.name(), "Fetched resumes successfully", resumes));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteResume(@PathVariable final Long id) {
    resumeService.deleteResume(id);
    return ResponseEntity.accepted()
        .body(ApiResponse.of(HttpStatus.ACCEPTED.name(), "Resume deleted successfully", null));
  }
}
