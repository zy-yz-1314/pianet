package com.pianet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** 文档：病人注册账号 */
@Getter
@Setter
public class RegisterRequest {

  @NotBlank
  @Size(min = 2, max = 50, message = "用户名长度需在 2~50 之间")
  private String username;

  @NotBlank
  @Size(min = 6, max = 100, message = "密码长度需在 6~100 之间")
  private String password;

  @NotBlank
  @Size(max = 50, message = "姓名长度不能超过 50")
  private String name;

  @NotBlank
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String phone;

  @NotBlank
  @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证格式不正确")
  private String idCard;

  private String gender;
  private LocalDate birthDate;
}
