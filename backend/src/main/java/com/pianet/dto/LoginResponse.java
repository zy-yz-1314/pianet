package com.pianet.dto;

import com.pianet.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

  private final String token;
  private final String username;
  private final String name;
  private final Role role;
  /** 病人角色绑定档案时使用 */
  private final Long patientId;
  /** 医生角色工作台使用 */
  private final Long doctorProfileId;
}
