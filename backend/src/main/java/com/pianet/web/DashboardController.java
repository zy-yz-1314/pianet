package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.service.DashboardService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping
  public Map<String, Object> overview(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return dashboardService.overview(uid);
  }

  @GetMapping("/charts")
  public Map<String, Object> charts(@RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return dashboardService.charts(uid);
  }
}
