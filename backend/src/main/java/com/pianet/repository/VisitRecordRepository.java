package com.pianet.repository;

import com.pianet.entity.VisitRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRecordRepository extends JpaRepository<VisitRecord, Long> {

  Page<VisitRecord> findByPatientIdOrderByVisitDateDescCreatedAtDesc(Long patientId, Pageable pageable);

  Page<VisitRecord> findAllByOrderByVisitDateDescCreatedAtDesc(Pageable pageable);

  long countByDoctorProfile_Id(Long doctorProfileId);

  long countByVisitDateBetween(java.time.LocalDate start, java.time.LocalDate end);

  long countByVisitDate(java.time.LocalDate date);
}
