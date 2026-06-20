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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor_profiles")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DoctorProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
  private SysUser user;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "department_id", nullable = false)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Department department;

  @Column(nullable = false, length = 50)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String introduction;

  private Integer workYears;
}
