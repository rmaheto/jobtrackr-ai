package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.dto.JobApplicationRequest;
import com.codemaniac.jobtrackrai.service.AiPromptService.PromptWithModel;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationAiService {

  private final ChatClient chatClient;
  private final AiPromptService promptService;

  /**
   * Extracts structured fields for a job application from scraped job text. Uses model from DB
   * template if present; otherwise falls back to the default model configured in application.yml
   * (spring.ai.openai.chat.options.model).
   */
  public JobApplicationRequest extractFromUrl(final String jobUrl, final String jobText) {
    final Map<String, Object> vars = Map.of("jobText", jobText);
    final PromptWithModel p = promptService.build("JOB_EXTRACTION", vars);

    final boolean b = p.model() != null && !p.model().isBlank();
    if (log.isDebugEnabled()) {
      log.debug("Built JOB_EXTRACTION prompt. Model override present? {}", b);
    }

    // If the DB template specifies a model, override it per-call via OpenAiChatOptions.
    if (b) {
      final OpenAiChatOptions options =
          OpenAiChatOptions.builder().model(p.model()).temperature(0.0).build();

      final JobApplicationRequest req =
          chatClient
              .prompt()
              .user(p.prompt())
              .options(options)
              .call()
              .entity(JobApplicationRequest.class);

      if (req != null) {
        req.setJobLink(Optional.ofNullable(jobUrl));
      }
      return req;
    }

    // Otherwise, use defaults from application.yml (no explicit options)
    final JobApplicationRequest req =
        chatClient.prompt().user(p.prompt()).call().entity(JobApplicationRequest.class);

    if (req != null) {
      req.setJobLink(Optional.ofNullable(jobUrl));
    }
    return req;
  }
}
