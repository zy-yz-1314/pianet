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
@Table(name = "ai_chats")
@Getter
@Setter
@NoArgsConstructor
public class AiChat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "patient_id", nullable = false)
  @JsonIgnoreProperties({"visits", "hibernateLazyInitializer", "handler"})
  private Patient patient;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String question;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String answer;

  @Column(nullable = false, updatable = false)
  private LocalDateTime chatTime;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "doctor_profile_id")
  @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
  private DoctorProfile doctorViewer;

  @PrePersist
  void prePersist() {
    if (chatTime == null) {
      chatTime = LocalDateTime.now();
    }
  }
}
