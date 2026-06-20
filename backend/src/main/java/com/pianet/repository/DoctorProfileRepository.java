package com.pianet.repository;

import com.pianet.entity.DoctorProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

  Optional<DoctorProfile> findByUser_Id(Long userId);
}
