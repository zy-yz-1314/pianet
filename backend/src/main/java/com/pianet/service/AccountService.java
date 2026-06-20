package com.pianet.service;

import com.pianet.dto.LoginResponse;
import com.pianet.entity.DoctorProfile;
import com.pianet.entity.Patient;
import com.pianet.entity.SysUser;
import com.pianet.repository.DoctorProfileRepository;
import com.pianet.repository.PatientRepository;
import com.pianet.repository.SysUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final SysUserRepository sysUserRepository;
  private final PatientRepository patientRepository;
  private final DoctorProfileRepository doctorProfileRepository;

  @Transactional(readOnly = true)
  public LoginResponse currentUser(Long userId) {
    SysUser u =
        sysUserRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
    Long patientId =
        patientRepository.findByLinkedUser_Id(u.getId()).map(Patient::getId).orElse(null);
    Long doctorProfileId =
        doctorProfileRepository.findByUser_Id(u.getId()).map(DoctorProfile::getId).orElse(null);
    return new LoginResponse(
        "", u.getUsername(), u.getName(), u.getRole(), patientId, doctorProfileId);
  }
}
