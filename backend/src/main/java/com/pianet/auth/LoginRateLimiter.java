package com.pianet.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

  private static final int MAX_ATTEMPTS = 5;
  private static final long WINDOW_MS = 60_000; // 1 分钟

  private final Map<String, AttemptWindow> cache = new ConcurrentHashMap<>();

  public boolean isBlocked(String ip) {
    AttemptWindow w = cache.get(ip);
    if (w == null) return false;
    long now = System.currentTimeMillis();
    // 窗口过期则清除
    if (now - w.windowStart > WINDOW_MS) {
      cache.remove(ip);
      return false;
    }
    return w.count >= MAX_ATTEMPTS;
  }

  public void recordAttempt(String ip) {
    long now = System.currentTimeMillis();
    cache.compute(ip, (k, v) -> {
      if (v == null || now - v.windowStart > WINDOW_MS) {
        return new AttemptWindow(now, 1);
      }
      return new AttemptWindow(v.windowStart, v.count + 1);
    });
  }

  private static class AttemptWindow {
    final long windowStart;
    final int count;

    AttemptWindow(long s, int c) {
      windowStart = s;
      count = c;
    }
  }
}
