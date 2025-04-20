package com.sns.project.repository;

import com.sns.project.domain.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);
  Optional<User> findByEmailAndPassword(String email, String password);

  boolean existsByEmail(String email);

  Optional<User> findByUserId(String userId);
  boolean existsByUserId(String userId);
}
