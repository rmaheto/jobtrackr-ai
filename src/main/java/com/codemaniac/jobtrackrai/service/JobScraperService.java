package com.codemaniac.jobtrackrai.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JobScraperService {

  public String extractJobText(final String url) {
    log.info("Scraping job description from {}", url);

    try (final Playwright playwright = Playwright.create()) {
      final Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      final Page page = browser.newPage();

      // Navigate to job URL
      page.navigate(url);

      // Wait until page fully loads job content
      page.waitForLoadState(LoadState.NETWORKIDLE);

      // Example selector for Indeed job description
      final String jobText = page.textContent(".jobsearch-JobComponent-description");

      if (jobText == null || jobText.isBlank()) {
        log.warn("No job description found at {}", url);
        return "";
      }

      log.info("Successfully extracted job text ({} chars)", jobText.length());
      return jobText.trim();
    } catch (final Exception e) {
      log.error("Failed to scrape job URL {}", url, e);
      throw new RuntimeException("Failed to scrape job URL", e);
    }
  }
}
