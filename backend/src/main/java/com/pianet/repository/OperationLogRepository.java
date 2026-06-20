package com.pianet.repository;

import com.pianet.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

  Page<OperationLog> findAllByOrderByOperationTimeDesc(Pageable pageable);
}
