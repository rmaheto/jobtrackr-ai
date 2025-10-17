package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.UserPreference;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
  Optional<UserPreference> findByUserId(Long userId);
}
