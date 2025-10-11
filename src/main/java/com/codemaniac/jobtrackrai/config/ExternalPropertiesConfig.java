package com.codemaniac.jobtrackrai.config;

import com.codemaniac.jobtrackrai.exception.PropertyEncryptionException;
import com.codemaniac.jobtrackrai.util.EncryptionUtil;
import jakarta.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

@Configuration
@Slf4j
class ExternalPropertiesConfig {

  private static final String ENCRYPTION_SECRET_KEY = "ENCRYPTION_SECRET_KEY";
  private static final String DOT_KEY = ".key";

  @Bean
  public static PropertySourcesPlaceholderConfigurer propertyConfigurer(
      final Environment environment) {

    final PropertySourcesPlaceholderConfigurer configurer =
        new PropertySourcesPlaceholderConfigurer();

    try {

      final String activeProfile = getActiveProfile(environment);
      final String externalFilePath = getExternalFilePath(activeProfile);
      final File externalFile = new File(externalFilePath);

      if (!externalFile.exists()) {
        log.warn(
            "External properties file not found: {}. Using internal properties.", externalFilePath);
        return configurer;
      }

      log.info("Loading external properties from: {}", externalFilePath);
      final Properties properties = loadPropertiesFromFile(externalFile);

      final String secretKey = getSecretKey(properties);
      encryptUnencryptedProperties(properties, secretKey, externalFile);
      final Properties decryptedProperties = decryptProperties(properties, secretKey);
      configurer.setProperties(decryptedProperties);
    } catch (final Exception e) {
      log.error("Error while loading external properties", e);
      throw new PropertyEncryptionException("Error while loading external properties", e);
    }

    configurer.setIgnoreResourceNotFound(true);
    configurer.setIgnoreUnresolvablePlaceholders(true);
    return configurer;
  }

  private static String getActiveProfile(@Nonnull final Environment environment) {
    return Optional.of(environment.getActiveProfiles())
        .filter(profiles -> profiles.length > 0)
        .map(profiles -> profiles[0])
        .orElse("default");
  }

  private static String getExternalFilePath(@Nonnull final String activeProfile) {
    return Paths.get(
            File.separator + "keys", "jobtrackr_ai_credentials_" + activeProfile + ".properties")
        .toAbsolutePath()
        .toString();
  }

  private static Properties loadPropertiesFromFile(@Nonnull final File externalFile) {
    final Properties properties = new Properties();
    try (final FileInputStream inputStream = new FileInputStream(externalFile)) {
      properties.load(inputStream);
    } catch (final IOException e) {
      log.error("Failed to load external properties: {}", e.getMessage());
      throw new PropertyEncryptionException("Failed to load external properties", e);
    }
    return properties;
  }

  private static String getSecretKey(@Nonnull final Properties properties) {
    String secretKey = System.getenv(ENCRYPTION_SECRET_KEY);

    if (secretKey == null || secretKey.isEmpty()) {
      secretKey = properties.getProperty(ENCRYPTION_SECRET_KEY);
    }

    if (secretKey == null || secretKey.isEmpty()) {
      throw new PropertyEncryptionException(
          "ENCRYPTION_SECRET_KEY is missing. Set it as an environment variable or in the properties file.");
    }

    return secretKey;
  }

  @SuppressWarnings("squid:S3329")
  private static void encryptUnencryptedProperties(
      @Nonnull final Properties properties,
      @Nonnull final String secretKey,
      @Nonnull final File externalFile) {

    boolean updated = false;

    for (final String key : properties.stringPropertyNames()) {
      final String value = properties.getProperty(key);

      if (key.toLowerCase(Locale.ROOT).endsWith(DOT_KEY) && !EncryptionUtil.isEncrypted(value)) {
        properties.setProperty(key, EncryptionUtil.encrypt(secretKey, value));
        updated = true;
        log.info("Encrypting property: {}", key);
      }
    }

    if (updated) {
      try (final FileOutputStream outputStream = new FileOutputStream(externalFile)) {
        properties.store(outputStream, "Updated with encrypted values");
        log.info("Updated properties file with encrypted values.");
      } catch (final IOException e) {
        log.error("Failed to update encrypted properties: {}", e.getMessage());
      }
    }
  }

  private static Properties decryptProperties(
      @Nonnull final Properties properties, @Nonnull final String secretKey) {
    final Properties decryptedProperties = new Properties();
    properties.forEach(
        (key, value) -> {
          final String keyStr = key.toString();
          final String valueStr = value.toString();
          if (keyStr.endsWith(DOT_KEY)) {
            decryptedProperties.setProperty(keyStr, EncryptionUtil.decrypt(secretKey, valueStr));
          } else {
            decryptedProperties.setProperty(keyStr, valueStr);
          }
        });
    return decryptedProperties;
  }
}
