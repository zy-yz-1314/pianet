package com.pianet.service;

import com.pianet.dto.AiChatAskRequest;
import com.pianet.entity.AiChat;
import com.pianet.entity.Patient;
import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import com.pianet.repository.AiChatRepository;
import com.pianet.repository.PatientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AiService {

  private static final Logger log = LoggerFactory.getLogger(AiService.class);

  private final AiChatRepository aiChatRepository;
  private final PatientRepository patientRepository;
  private final AccessService accessService;
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  @Value("${pianet.deepseek.api-key:}")
  private String apiKey;

  @Value("${pianet.deepseek.url:https://api.deepseek.com/v1/chat/completions}")
  private String deepseekUrl;

  @Value(
      "${pianet.deepseek.system-prompt:你是专业医疗咨询助手，仅提供健康咨询、症状分析与科普，不进行确诊。"
          + "若症状严重须提示就医。}")
  private String systemPrompt;

  @Transactional(readOnly = true)
  public List<AiChat> history(Long patientId, Long viewerId) {
    assertCanViewPatientAi(patientId, viewerId);
    return aiChatRepository.findByPatientIdOrderByChatTimeDesc(patientId);
  }

  private void assertCanViewPatientAi(Long patientId, Long viewerId) {
    SysUser u = accessService.requireUser(viewerId);
    if (u.getRole() == Role.PATIENT) {
      Patient mine =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
      if (!mine.getId().equals(patientId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能查看他人咨询记录");
      }
    }
    // 医生 / 管理员可查看任意病人 AI 记录（辅助接诊）
  }

  @Transactional
  public AiChat ask(AiChatAskRequest req, Long viewerId) {
    assertCanViewPatientAi(req.getPatientId(), viewerId);
    Patient p =
        patientRepository
            .findById(req.getPatientId())
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("病人不存在"));
    String q = req.getQuestion().trim();
    if (q.length() < 2) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请输入合法的健康咨询问题");
    }
    String answer = callDeepSeek(q);
    AiChat c = new AiChat();
    c.setPatient(p);
    c.setQuestion(q);
    c.setAnswer(answer);
    return aiChatRepository.save(c);
  }

  private String callDeepSeek(String userQuestion) {
    if (apiKey == null || apiKey.isBlank()) {
      return "【演示模式】未配置 DeepSeek API Key。请在 backend application.yml 中设置 pianet.deepseek.api-key。"
          + " 您的问题已记录为："
          + userQuestion;
    }
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);
      Map<String, Object> body =
          Map.of(
              "model",
              "deepseek-chat",
              "messages",
              List.of(
                  Map.of("role", "system", "content", systemPrompt),
                  Map.of("role", "user", "content", userQuestion)));
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
      String raw = restTemplate.postForObject(deepseekUrl, entity, String.class);
      JsonNode root = objectMapper.readTree(raw);
      return root.path("choices").get(0).path("message").path("content").asText("（无返回内容）");
    } catch (Exception e) {
      log.error("DeepSeek API 调用失败", e);
      return "AI 咨询暂时不可用，请稍后再试。";
    }
  }
}
