package com.codemaniac.jobtrackrai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeDto {
  private Long id;
  private String originalName;
  private String fileType;
  private Long size;
  private DateRepresentation uploadDate;
  private Integer linkedApplications;
  private Long userId;
  private String previewUrl;
  private List<JobApplicationSummaryDto> jobApplications;
}
