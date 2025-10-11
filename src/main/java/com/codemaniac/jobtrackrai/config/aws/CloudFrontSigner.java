package com.codemaniac.jobtrackrai.config.aws;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CloudFrontSigner {

  @Value("${aws.cloudfront.keyPairId}")
  private String keyPairId;

  @Value("${aws.cloudfront.privateKeyPath}")
  private String privateKeyPath;

  @Value("${aws.cloudfront.domain}")
  private String cfDomain;

  private PrivateKey loadPrivateKey() {

    final File pemFile = new File(privateKeyPath);
    if (!pemFile.exists()) {
      throw new IllegalStateException(
          "Private key file not found at: " + pemFile.getAbsolutePath());
    }

    try (final FileInputStream fis = new FileInputStream(pemFile)) {
      final String pemContent = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
      final String cleanBase64 = extractPemContent(pemContent); // removes headers and whitespace

      final byte[] decoded = Base64.getDecoder().decode(cleanBase64);
      return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));

    } catch (final Exception e) {
      log.error("Error loading CloudFront private key", e);
      throw new IllegalStateException("Unable to load CloudFront private key", e);
    }
  }

  private String extractPemContent(final String pem) {
    return pem.replaceAll("-----BEGIN [A-Z ]+-----", "")
        .replaceAll("-----END [A-Z ]+-----", "")
        .replaceAll("\\s+", "");
  }

  public String createSignedUrl(final String objectKey, final Instant expiresAt) {

    final PrivateKey pk = loadPrivateKey();
    final String resourceUrl = "https://" + cfDomain + "/" + objectKey;

    return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
        resourceUrl, keyPairId, pk, Date.from(expiresAt));
  }
}
