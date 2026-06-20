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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registrations")
@Getter
@Setter
@NoArgsConstructor
public class Registration {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_id", nullable = false)
  @JsonIgnoreProperties({"visits", "hibernateLazyInitializer", "handler"})
  private Patient patient;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_profile_id", nullable = false)
  @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
  private DoctorProfile doctorProfile;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "schedule_id", nullable = false)
  @JsonIgnoreProperties({"doctorProfile", "hibernateLazyInitializer", "handler"})
  private Schedule schedule;

  @Column(nullable = false, unique = true, length = 50)
  private String registrationNo;

  /** 待就诊 / 就诊中 / 已完成 / 已取消 */
  @Column(nullable = false, length = 20)
  private String status = "待就诊";

  @Column(nullable = false)
  private int queueNo;

  @Column(nullable = false, updatable = false)
  private LocalDateTime registrationTime;

  private LocalDateTime cancelTime;

  private LocalDateTime visitTime;

  @PrePersist
  void prePersist() {
    if (registrationTime == null) {
      registrationTime = LocalDateTime.now();
    }
  }
}
