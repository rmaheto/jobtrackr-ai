package com.codemaniac.jobtrackrai.service.aws;

import com.codemaniac.jobtrackrai.config.aws.AwsProperties;
import com.codemaniac.jobtrackrai.exception.InternalServerException;
import com.codemaniac.jobtrackrai.exception.S3UploadException;
import jakarta.annotation.Nonnull;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;
  private final AwsProperties awsProperties;

  /** Uploads a file to S3 and returns the object key. */
  public String uploadFile(
      @Nonnull final String bucketName,
      @Nonnull final String key,
      @Nonnull final InputStream inputStream,
      @Nonnull final Long contentLength,
      @Nonnull final String contentType) {

    final List<String> allowedTypes = awsProperties.getUploads().getContentType();
    if (!allowedTypes.contains(contentType)) {
      log.warn("Upload blocked. Unsupported content type: {}", contentType);
      throw new S3UploadException("Unsupported content type: " + contentType);
    }

    try {
      log.debug("Uploading to S3: bucket={}, key={}, size={}", bucketName, key, contentLength);

      final PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentLength(contentLength)
              .contentType(contentType)
              .cacheControl(awsProperties.getCacheControl())
              .build();

      s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

      return key;
    } catch (final Exception e) {
      log.error("S3 upload failed for key: {}", key, e);
      throw new InternalServerException("File upload failed:" + e);
    }
  }

  public void deleteFile(@Nonnull final String bucketName, @Nonnull final String key) {
    try {
      log.debug("Deleting from S3: bucket={}, key={}", bucketName, key);

      final DeleteObjectRequest request =
          DeleteObjectRequest.builder().bucket(bucketName).key(key).build();

      s3Client.deleteObject(request);
    } catch (final Exception e) {
      log.error("S3 delete failed for key: {}", key, e);
      throw new InternalServerException("File deletion failed: " + e);
    }
  }
}
