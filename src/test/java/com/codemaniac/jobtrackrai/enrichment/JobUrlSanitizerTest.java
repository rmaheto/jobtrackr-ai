package com.codemaniac.jobtrackrai.enrichment;



import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class JobUrlSanitizerTest {

  @Test
  void sanitize_whenNullUrl_returnEmpty() {
    final Optional<String> result = JobUrlSanitizer.sanitize(null);

    assertTrue(result.isEmpty());
  }

  @Test
  void sanitize_whenBlankUrl_returnEmpty() {
    final Optional<String> result = JobUrlSanitizer.sanitize("   ");

    assertTrue(result.isEmpty());
  }

  @Test
  void sanitize_whenIndeedSearchUrlWithVjk_returnCanonicalViewJobUrl() {
    final String rawUrl =
        "https://www.indeed.com/jobs?q=software+engineer"
            + "&vjk=ae6c06b5dea7f9c6"
            + "&from=searchOnHP";

    final Optional<String> result = JobUrlSanitizer.sanitize(rawUrl);

    assertTrue(result.isPresent());
    assertEquals("https://www.indeed.com/viewjob?jk=ae6c06b5dea7f9c6", result.get());
  }

  @Test
  void sanitize_whenIndeedViewJobUrl_returnSameCanonicalUrl() {
    final String canonicalUrl = "https://www.indeed.com/viewjob?jk=ae6c06b5dea7f9c6";

    final Optional<String> result = JobUrlSanitizer.sanitize(canonicalUrl);

    assertTrue(result.isPresent());
    assertEquals(canonicalUrl, result.get());
  }

  @Test
  void sanitize_whenIndeedUrlWithoutJobKey_returnEmpty() {
    final String rawUrl = "https://www.indeed.com/jobs?q=software+engineer";

    final Optional<String> result = JobUrlSanitizer.sanitize(rawUrl);

    assertTrue(result.isEmpty());
  }

  @Test
  void sanitize_whenNonIndeedUrl_returnOriginalUrl() {
    final String rawUrl = "https://company.com/jobs/123";

    final Optional<String> result = JobUrlSanitizer.sanitize(rawUrl);

    assertTrue(result.isPresent());
    assertEquals(rawUrl, result.get());
  }

  @Test
  void sanitize_whenMalformedNonIndeedUrl_returnOriginalUrl() {
    final String rawUrl = "ht!tp://indeed..com%%%";

    final Optional<String> result = JobUrlSanitizer.sanitize(rawUrl);

    assertTrue(result.isPresent());
    assertEquals(rawUrl, result.get());
  }
}
