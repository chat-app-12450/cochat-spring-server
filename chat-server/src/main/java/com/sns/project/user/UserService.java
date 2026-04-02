package com.sns.project.user;

import com.sns.project.core.domain.user.User;
import com.sns.project.core.domain.user.UserFactory;
import com.sns.project.core.exception.badRequest.RegisterFailedException;
import com.sns.project.core.exception.notfound.NotFoundUserException;
import com.sns.project.core.exception.unauthorized.InvalidPasswordException;
import com.sns.project.core.repository.user.UserRepository;
import com.sns.project.user.dto.request.RequestRegisterDto;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.webjars.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public User register(RequestRegisterDto request) {
    validateEmail(request.getEmail());
    validateUserId(request.getUserId());

    User newUser = UserFactory.createUser(
        request.getEmail(),
        request.getUserId(),
        request.getPassword(),
        request.getName()
    );
    newUser.setPassword(hashPassword(newUser.getPassword()));

    User registered = userRepository.save(newUser);
    log.info("사용자 등록 성공: {}", registered.getEmail());
    return registered;
  }

  @Transactional
  public User authenticate(String userId, String password) {
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(userId + ": 존재하지 않는 유저 아이디 입니다."));

    if (!BCrypt.checkpw(password, user.getPassword())) {
      throw new InvalidPasswordException();
    }

    return user;
  }

  public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new NotFoundUserException("user id(" + id + ")does not exist"));
  }

  public List<User> getUsersByIds(Set<Long> participantIds) {
    return userRepository.findAllById(participantIds);
  }

  public void findByUserId(String userId) {
    userRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundUserException(userId));
  }

  private String hashPassword(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  private void validateEmail(String email) {
    if (userRepository.existsByEmail(email)) {
      log.warn("Registration failed: Email already exists - {}", email);
      throw new RegisterFailedException("이미 사용 중인 이메일입니다");
    }
  }

  private void validateUserId(String userId) {
    if (userRepository.existsByUserId(userId)) {
      log.warn("Registration failed: User ID already exists - {}", userId);
      throw new RegisterFailedException("이미 사용 중인 사용자 ID입니다");
    }
  }
}
