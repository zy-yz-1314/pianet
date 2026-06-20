package com.pianet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationStatusRequest {

  @NotBlank private String status;
}
