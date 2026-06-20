package com.pianet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRequest {

  @NotBlank
  @Size(max = 64, message = "姓名长度不能超过 64")
  private String name;

  private Long departmentId;
  private String gender;
  private LocalDate birthDate;
  private Integer age;

  @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
  private String phone;

  @Pattern(regexp = "^$|^\\d{17}[\\dXx]$", message = "身份证格式不正确")
  private String idCard;

  @Size(max = 200, message = "地址长度不能超过 200")
  private String address;

  private String allergyHistory;
  private String pastMedicalHistory;
  private String familyMedicalHistory;
  private String medicationHistory;

  /** 是否归档（长期未就诊标记） */
  private Boolean archived;
}
