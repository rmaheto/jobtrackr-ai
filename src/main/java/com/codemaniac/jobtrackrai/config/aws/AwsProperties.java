package com.codemaniac.jobtrackrai.config.aws;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class AwsProperties {
  private String region;
  private int timeToLive;
  private String bucket;
  private String cacheControl;
  private UploadSettings uploads;

  @Getter
  @Setter
  public static class UploadSettings {
    private List<String> contentType;
  }
}
