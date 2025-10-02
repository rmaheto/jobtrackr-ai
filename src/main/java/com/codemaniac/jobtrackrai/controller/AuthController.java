package com.codemaniac.jobtrackrai.controller;

import com.codemaniac.jobtrackrai.dto.UserDto;
import com.codemaniac.jobtrackrai.entity.User;
import com.codemaniac.jobtrackrai.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final CurrentUserService currentUserService;

  public AuthController(final CurrentUserService currentUserService) {
    this.currentUserService = currentUserService;
  }

  @GetMapping("/me")
  public ResponseEntity<UserDto> me() {
    final User user = currentUserService.getCurrentUser();
    return ResponseEntity.ok(new UserDto(user.getId(), user.getEmail(), user.getName()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      final HttpServletRequest request, final HttpServletResponse response) {
    final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    return ResponseEntity.noContent().build();
  }
}
