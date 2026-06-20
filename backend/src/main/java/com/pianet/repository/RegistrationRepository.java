package com.pianet.repository;

import com.pianet.entity.Registration;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

  Page<Registration> findByDoctorProfileIdOrderByRegistrationTimeAsc(Long doctorProfileId, Pageable pageable);

  Page<Registration> findByPatientIdOrderByRegistrationTimeDesc(Long patientId, Pageable pageable);

  Page<Registration> findAllByOrderByRegistrationTimeDesc(Pageable pageable);

  long countByStatusAndRegistrationTimeBetween(String status, LocalDateTime start, LocalDateTime end);

  long countByScheduleId(Long scheduleId);

  long countByDoctorProfileIdAndStatus(Long doctorProfileId, String status);

  long countByStatus(String status);
}
