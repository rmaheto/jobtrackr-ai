package com.codemaniac.jobtrackrai.config.aws;

import com.amazonaws.regions.Regions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
public class AwsConfig {

  @Bean
  public S3Client s3Client(final AwsProperties props) {
    return S3Client.builder()
        .region(Region.of(props.getRegion()))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  @Bean
  public S3Presigner s3Presigner(final AwsProperties props) {
    return S3Presigner.builder()
        .region(Region.of(props.getRegion()))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }

  @Bean(name = "amazonRegion")
  public com.amazonaws.regions.Region amazonRegion() {
    com.amazonaws.regions.Region s3Region = Regions.getCurrentRegion();
    if (s3Region != null) {
      log.info("S3 region from current region name is: {}", s3Region.getName().toLowerCase());
    }

    if (s3Region == null) {
      s3Region = com.amazonaws.regions.Region.getRegion(Regions.US_EAST_2);
      log.info("Using default region: {}", s3Region.getName().toLowerCase());
    }

    return s3Region;
  }
}
