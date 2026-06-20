package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.LoginResponse;
import com.pianet.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

  private final AccountService accountService;

  @GetMapping
  public LoginResponse me(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return accountService.currentUser(userId);
  }
}
