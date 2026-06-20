package com.pianet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatAskRequest {

  @NotNull private Long patientId;

  @NotBlank
  @Size(min = 2, max = 2000, message = "问题长度需在 2~2000 之间")
  private String question;
}
