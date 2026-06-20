package com.pianet.dto;

import com.pianet.entity.VisitRecord;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PatientDetailResponse {

  private final Long id;
  private final Long departmentId;
  private final String departmentName;
  private final String name;
  private final String gender;
  private final LocalDate birthDate;
  private final Integer age;
  private final String phone;
  private final String idCard;
  private final String address;
  private final String allergyHistory;
  private final String pastMedicalHistory;
  private final String familyMedicalHistory;
  private final String medicationHistory;
  private final boolean archived;
  private final List<VisitRecord> visits;
}
