package com.pianet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Patient {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 可与登录账号绑定（病人角色）；线下建档可为空 */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "linked_user_id", unique = true)
  @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
  private SysUser linkedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "department_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private Department department;

  @NotBlank
  @Column(nullable = false, length = 64)
  private String name;

  @Column(length = 8)
  private String gender;

  private LocalDate birthDate;

  private Integer age;

  @Column(length = 32)
  private String phone;

  @Column(length = 32)
  private String idCard;

  @Column(length = 200)
  private String address;

  @Column(columnDefinition = "TEXT")
  private String allergyHistory;

  @Column(columnDefinition = "TEXT")
  private String pastMedicalHistory;

  @Column(columnDefinition = "TEXT")
  private String familyMedicalHistory;

  @Column(columnDefinition = "TEXT")
  private String medicationHistory;

  /** 归档：不在常规列表默认展示（后续可筛选） */
  @Column(nullable = false)
  private boolean archived = false;

  @JsonIgnore
  @OrderBy("visitDate desc, createdAt desc")
  @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<VisitRecord> visits = new ArrayList<>();
}
