package com.pianet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationBookRequest {

  @NotNull private Long scheduleId;
  @NotNull private Long patientId;
}
