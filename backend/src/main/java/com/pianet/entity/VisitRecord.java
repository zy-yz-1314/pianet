package com.pianet.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visit_records")
@Getter
@Setter
@NoArgsConstructor
public class VisitRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_id", nullable = false)
  @JsonIgnoreProperties({
    "visits",
    "linkedUser",
    "hibernateLazyInitializer",
    "handler"
  })
  private Patient patient;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_profile_id")
  @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
  private DoctorProfile doctorProfile;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "registration_id")
  @JsonIgnoreProperties({
    "patient",
    "doctorProfile",
    "schedule",
    "hibernateLazyInitializer",
    "handler"
  })
  private Registration registration;

  private LocalDate visitDate;

  @Column(length = 64)
  private String department;

  @Column(length = 64)
  private String doctorName;

  /** 症状描述（对应文档 symptoms，库表沿用 chief_complaint 列以兼容已有数据） */
  @Column(name = "chief_complaint", columnDefinition = "TEXT")
  private String symptoms;

  @Column(length = 200)
  private String physicalSigns;

  @Column(columnDefinition = "TEXT")
  private String diagnosis;

  @Column(columnDefinition = "TEXT")
  private String prescription;

  /** 医嘱、复诊提醒 */
  @Column(columnDefinition = "TEXT")
  private String advice;

  @Column(length = 1024)
  private String notes;

  /** 就诊中 / 已完成 */
  @Column(nullable = false, length = 20)
  private String visitStatus = "已完成";

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    updatedAt = createdAt;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
