package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.auth.LoginRateLimiter;
import com.pianet.dto.LoginRequest;
import com.pianet.dto.LoginResponse;
import com.pianet.dto.RegisterRequest;
import com.pianet.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final LoginRateLimiter rateLimiter;

  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public LoginResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request) {
    String ip = request.getRemoteAddr();
    if (rateLimiter.isBlocked(ip)) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "登录尝试过于频繁，请稍后再试");
    }
    rateLimiter.recordAttempt(ip);
    return authService.login(body);
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public LoginResponse register(@Valid @RequestBody RegisterRequest body) {
    return authService.registerPatient(body);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userIdIgnored) {
    authService.logout(authorization);
  }
}
