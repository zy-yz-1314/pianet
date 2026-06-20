package com.pianet.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

  private static final Logger log = LoggerFactory.getLogger(TokenStore.class);
  private static final long TOKEN_TTL_HOURS = 24;

  private final ConcurrentHashMap<String, TokenEntry> store = new ConcurrentHashMap<>();

  private record TokenEntry(Long userId, Instant expiresAt) {}

  public String createToken(Long userId) {
    String token = UUID.randomUUID().toString();
    store.put(token, new TokenEntry(userId, Instant.now().plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS)));
    return token;
  }

  public Optional<Long> resolve(String token) {
    if (token == null || token.isBlank()) {
      return Optional.empty();
    }
    TokenEntry entry = store.get(token.strip());
    if (entry == null) {
      return Optional.empty();
    }
    // 过期自动移除
    if (Instant.now().isAfter(entry.expiresAt)) {
      store.remove(token.strip());
      return Optional.empty();
    }
    return Optional.of(entry.userId);
  }

  public void invalidate(String token) {
    if (token != null) {
      store.remove(token.strip());
    }
  }

  /** 每 30 分钟清理过期 token */
  @Scheduled(fixedRate = 1_800_000)
  public void cleanExpired() {
    Instant now = Instant.now();
    store.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt));
    log.debug("Token 过期清理完成，当前有效 token 数：{}", store.size());
  }
}
