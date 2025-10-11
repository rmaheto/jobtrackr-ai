package com.codemaniac.jobtrackrai.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.codemaniac.jobtrackrai.config.aws.CloudFrontSigner;
import com.codemaniac.jobtrackrai.dto.ResumeDto;
import com.codemaniac.jobtrackrai.entity.Resume;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.enums.ResumeFileType;
import com.codemaniac.jobtrackrai.exception.S3UploadException;
import com.codemaniac.jobtrackrai.mapper.ResumeMapper;
import com.codemaniac.jobtrackrai.repository.ResumeRepository;
import com.codemaniac.jobtrackrai.service.aws.S3Service;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

  @Mock private ResumeRepository resumeRepository;
  @Mock private S3Service s3Service;
  @Mock private CurrentUserService currentUserService;
  @Mock private CloudFrontSigner cloudFrontSigner;
  @Mock private ResumeMapper resumeMapper;

  @InjectMocks private ResumeService resumeService;

  private User mockUser;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(resumeService, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(resumeService, "maxFileSize", 5 * 1024 * 1024L);
    mockUser = new User();
    mockUser.setId(1L);
  }

  @Test
  void uploadResume_whenValidFile_shouldUploadSuccessfully() {
    final MultipartFile file =
        new MockMultipartFile("file", "resume.pdf", "application/pdf", "PDF content".getBytes());

    final Resume resume =
        Resume.builder()
            .id(10L)
            .originalName("resume.pdf")
            .fileType(ResumeFileType.PDF)
            .size(file.getSize())
            .user(mockUser)
            .build();
    when(currentUserService.getCurrentUser()).thenReturn(mockUser);
    when(resumeRepository.findByUserAndOriginalName(mockUser, "resume.pdf"))
        .thenReturn(Optional.empty());
    when(resumeRepository.save(any(Resume.class))).thenReturn(resume);

    final ResumeDto dto = new ResumeDto();
    dto.setId(10L);
    when(resumeMapper.toDto(resume)).thenReturn(dto);
    when(cloudFrontSigner.createSignedUrl(anyString(), any(Instant.class)))
        .thenReturn("signed-url");

    final ResumeDto result = resumeService.uploadResume(file);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(10L);
    assertThat(result.getPreviewUrl()).isEqualTo("signed-url");

    verify(s3Service)
        .uploadFile(
            eq("test-bucket"),
            contains("resume.pdf"),
            any(ByteArrayInputStream.class),
            eq(file.getSize()),
            eq("application/pdf"));
    verify(resumeRepository, times(1)).save(any(Resume.class));
  }

  @Test
  void uploadResume_whenInvalidFileType_shouldThrowException() {
    final MultipartFile file =
        new MockMultipartFile("file", "resume.txt", "text/plain", "invalid".getBytes());

    assertThatThrownBy(() -> resumeService.uploadResume(file))
        .isInstanceOf(S3UploadException.class)
        .hasMessageContaining("Only PDF and DOCX resumes are allowed");

    verifyNoInteractions(s3Service);
  }

  @Test
  void uploadResume_whenFileExceedsMaxSize_shouldThrowException() {
    final byte[] largeBytes = new byte[(int) (resumeService.getMaxFileSize() + 1)];
    final MultipartFile file =
        new MockMultipartFile("file", "bigresume.pdf", "application/pdf", largeBytes);

    assertThatThrownBy(() -> resumeService.uploadResume(file))
        .isInstanceOf(S3UploadException.class)
        .hasMessageContaining("File size exceeds maximum limit");
  }

  @Test
  void listResumes_whenUserHasResumes_shouldReturnDtosWithSignedUrls() {
    final Resume resume =
        Resume.builder()
            .id(1L)
            .originalName("resume.pdf")
            .s3Key("resumes/1/resume.pdf")
            .user(mockUser)
            .build();

    when(currentUserService.getCurrentUser()).thenReturn(mockUser);
    when(resumeRepository.findByUser(mockUser)).thenReturn(List.of(resume));
    final ResumeDto dto = new ResumeDto();
    dto.setId(1L);
    when(resumeMapper.toDto(resume)).thenReturn(dto);
    when(cloudFrontSigner.createSignedUrl(anyString(), any(Instant.class)))
        .thenReturn("signed-url");

    final List<ResumeDto> result = resumeService.listResumes();

    assertThat(result.get(0).getPreviewUrl()).isEqualTo("signed-url");

    verify(resumeRepository).findByUser(mockUser);
  }

  @Test
  void deleteResume_whenResumeOwnedByUser_shouldDeleteSuccessfully() {
    final Resume resume =
        Resume.builder()
            .id(1L)
            .originalName("resume.pdf")
            .s3Key("resumes/1/resume.pdf")
            .user(mockUser)
            .build();
    when(currentUserService.getCurrentUser()).thenReturn(mockUser);
    when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

    resumeService.deleteResume(1L);

    verify(s3Service).deleteFile("test-bucket", "resumes/1/resume.pdf");
    verify(resumeRepository).delete(resume);
  }

  @Test
  void deleteResume_whenResumeNotOwnedByUser_shouldThrowException() {
    final User otherUser = new User();
    otherUser.setId(2L);

    final Resume resume =
        Resume.builder().id(1L).user(otherUser).s3Key("resumes/2/resume.pdf").build();

    when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

    assertThatThrownBy(() -> resumeService.deleteResume(1L))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Resume not found or not owned by user");

    verifyNoInteractions(s3Service);
  }
}
