package com.pianet.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

  public static final String ATTR_USER_ID = "LOGIN_USER_ID";
  public static final String ATTR_USERNAME = "LOGIN_USERNAME";

  private final TokenStore tokenStore;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    String auth = request.getHeader("Authorization");
    String token =
        Optional.ofNullable(auth)
            .filter(a -> a.startsWith("Bearer "))
            .map(a -> a.substring(7))
            .orElse("");
    Optional<Long> uid = tokenStore.resolve(token);
    if (uid.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return false;
    }
    request.setAttribute(ATTR_USER_ID, uid.get());
    return true;
  }
}
