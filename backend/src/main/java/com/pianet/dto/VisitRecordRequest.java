package com.pianet.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisitRecordRequest {

  @NotNull private Long patientId;
  private Long registrationId;
  private Long doctorProfileId;
  private LocalDate visitDate;
  private String department;
  private String doctorName;
  /** 症状描述 */
  private String symptoms;
  /** 兼容旧前端字段 */
  private String chiefComplaint;
  private String physicalSigns;
  private String diagnosis;
  private String prescription;
  /** 医嘱、复诊提醒 */
  private String advice;
  /** 就诊中 / 已完成 */
  private String visitStatus;
  private String notes;
}
