package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.AiChatAskRequest;
import com.pianet.entity.AiChat;
import com.pianet.service.AiService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

  private final AiService aiService;

  @GetMapping("/history/{patientId}")
  public List<AiChat> history(
      @PathVariable Long patientId, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return aiService.history(patientId, uid);
  }

  @PostMapping("/ask")
  @ResponseStatus(HttpStatus.CREATED)
  public AiChat ask(
      @Valid @RequestBody AiChatAskRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return aiService.ask(body, uid);
  }
}
