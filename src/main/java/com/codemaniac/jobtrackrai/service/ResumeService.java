package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.config.aws.CloudFrontSigner;
import com.codemaniac.jobtrackrai.dto.ResumeDto;
import com.codemaniac.jobtrackrai.entity.Resume;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.ResumeFileType;
import com.codemaniac.jobtrackrai.exception.S3UploadException;
import com.codemaniac.jobtrackrai.mapper.ResumeMapper;
import com.codemaniac.jobtrackrai.repository.ResumeRepository;
import com.codemaniac.jobtrackrai.service.aws.S3Service;
import jakarta.annotation.Nonnull;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResumeService {

  private final ResumeRepository resumeRepository;
  private final S3Service s3Service;
  private final CurrentUserService currentUserService;
  private final CloudFrontSigner cloudFrontSigner;
  private final ResumeMapper resumeMapper;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  @Value("${resume.maxFileSize}")
  @Getter
  private long maxFileSize;

  @Transactional
  public ResumeDto uploadResume(@Nonnull final MultipartFile file) {
    validateFile(file);

    final User user = currentUserService.getCurrentUser();

    final Resume resume =
        resumeRepository
            .findByUserAndOriginalName(user, file.getOriginalFilename())
            .orElse(
                Resume.builder()
                    .originalName(file.getOriginalFilename())
                    .user(user)
                    .linkedApplications(0)
                    .build());

    resume.setFileType(ResumeFileType.fromMimeType(Objects.requireNonNull(file.getContentType())));
    resume.setSize(file.getSize());

    final String key = String.format("resumes/%d/%s", user.getId(), file.getOriginalFilename());

    try (final InputStream is = file.getInputStream()) {
      s3Service.uploadFile(bucketName, key, is, file.getSize(), file.getContentType());
    } catch (final Exception e) {
      throw new S3UploadException("Failed to upload resume", e);
    }

    resume.setS3Key(key);
    final Resume saved = resumeRepository.save(resume);

    final ResumeDto dto = resumeMapper.toDto(saved);
    dto.setPreviewUrl(
        cloudFrontSigner.createSignedUrl(key, Instant.now().plus(15, ChronoUnit.MINUTES)));
    return dto;
  }

  public List<ResumeDto> listResumes() {
    final User user = currentUserService.getCurrentUser();

    return resumeRepository.findByUser(user).stream()
        .map(
            resume -> {
              final ResumeDto dto = resumeMapper.toDto(resume);
              dto.setPreviewUrl(
                  cloudFrontSigner.createSignedUrl(
                      resume.getS3Key(), Instant.now().plus(15, ChronoUnit.MINUTES)));
              return dto;
            })
        .toList();
  }

  @Transactional
  public void deleteResume(final Long id) {
    final User user = currentUserService.getCurrentUser();

    final Resume resume =
        resumeRepository
            .findById(id)
            .filter(r -> r.getUser().equals(user))
            .orElseThrow(() -> new RuntimeException("Resume not found or not owned by user"));

    s3Service.deleteFile(bucketName, resume.getS3Key());
    resumeRepository.delete(resume);
  }

  private void validateFile(@Nonnull final MultipartFile file) {
    final String contentType = file.getContentType();
    if (contentType == null
        || !(contentType.equals("application/pdf")
            || contentType.equals(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
      throw new S3UploadException("Only PDF and DOCX resumes are allowed");
    }

    if (file.getSize() > maxFileSize) {
      throw new S3UploadException(
          String.format("File size exceeds maximum limit of %d MB", maxFileSize / (1024 * 1024)));
    }
  }
}
