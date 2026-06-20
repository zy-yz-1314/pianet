package com.pianet.entity;

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
@Table(name = "operation_logs")
@Getter
@Setter
@NoArgsConstructor
public class OperationLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", nullable = false)
  private SysUser user;

  @Column(nullable = false, length = 100)
  private String operation;

  @Column(nullable = false, updatable = false)
  private LocalDateTime operationTime;

  @Column(length = 50)
  private String ipAddress;

  @Column(length = 200)
  private String remark;

  @PrePersist
  void prePersist() {
    if (operationTime == null) {
      operationTime = LocalDateTime.now();
    }
  }
}
