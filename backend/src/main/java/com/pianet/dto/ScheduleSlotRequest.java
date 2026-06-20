package com.pianet.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleSlotRequest {

  /** 管理员代建时必填；医生自建时后端按当前账号解析 */
  private Long doctorProfileId;

  @NotNull private LocalDate workDate;
  @NotNull private LocalTime startTime;
  @NotNull private LocalTime endTime;

  /** 默认可约 10 */
  private Integer maxNum = 10;
}
