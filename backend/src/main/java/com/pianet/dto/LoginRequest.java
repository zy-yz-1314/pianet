package com.pianet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  @NotBlank
  @Size(min = 2, max = 50, message = "用户名长度需在 2~50 之间")
  private String username;

  @NotBlank
  @Size(min = 6, max = 100, message = "密码长度需在 6~100 之间")
  private String password;
}
