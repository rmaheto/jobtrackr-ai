package com.codemaniac.jobtrackrai.service;

import com.codemaniac.jobtrackrai.entity.AiPromptTemplate;
import com.codemaniac.jobtrackrai.repository.AiPromptTemplateRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptService {

  private final AiPromptTemplateRepository repo;

  /**
   * Builds a prompt from a template stored in the DB.
   *
   * @param code prompt code (e.g. JOB_EXTRACTION)
   * @param vars variables to substitute {{key}} → value
   * @return prompt + model
   */
  public PromptWithModel build(final String code, final Map<String, Object> vars) {
    final AiPromptTemplate tpl =
        repo.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Prompt not found: " + code));

    String txt = tpl.getTemplate();
    for (final var e : vars.entrySet()) {
      txt = txt.replace("{{" + e.getKey() + "}}", String.valueOf(e.getValue()));
    }
    return new PromptWithModel(txt, tpl.getModel());
  }

  public record PromptWithModel(String prompt, String model) {}
}
