package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.AiPromptTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiPromptTemplateRepository extends JpaRepository<AiPromptTemplate, Long> {
  Optional<AiPromptTemplate> findByCode(String code);
}
