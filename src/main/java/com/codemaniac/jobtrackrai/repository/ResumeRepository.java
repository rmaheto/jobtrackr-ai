package com.codemaniac.jobtrackrai.repository;

import com.codemaniac.jobtrackrai.entity.Resume;
import com.codemaniac.jobtrackrai.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

  List<Resume> findByUser(User user);

  Optional<Resume> findByUserAndOriginalName(User user, String originalName);
}
