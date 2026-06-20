package com.pianet.service;

import com.pianet.auth.TokenStore;
import com.pianet.dto.LoginRequest;
import com.pianet.dto.LoginResponse;
import com.pianet.dto.RegisterRequest;
import com.pianet.entity.DoctorProfile;
import com.pianet.entity.Patient;
import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import com.pianet.repository.DoctorProfileRepository;
import com.pianet.repository.PatientRepository;
import com.pianet.repository.SysUserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final SysUserRepository sysUserRepository;
  private final PatientRepository patientRepository;
  private final DoctorProfileRepository doctorProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenStore tokenStore;

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest req) {
    SysUser user =
        sysUserRepository
            .findByUsername(req.getUsername().trim())
            .orElseThrow(() -> new EntityNotFoundException("用户名或密码错误"));
    if (!user.isEnabled()
        || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
      throw new EntityNotFoundException("用户名或密码错误");
    }

    Long patientId =
        patientRepository.findByLinkedUser_Id(user.getId()).map(Patient::getId).orElse(null);
    Long doctorProfileId =
        doctorProfileRepository.findByUser_Id(user.getId()).map(DoctorProfile::getId).orElse(null);

    String token = tokenStore.createToken(user.getId());
    return new LoginResponse(
        token, user.getUsername(), user.getName(), user.getRole(), patientId, doctorProfileId);
  }

  @Transactional
  public void logout(String bearerToken) {
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      tokenStore.invalidate(bearerToken.substring(7));
    }
  }

  @Transactional
  public LoginResponse registerPatient(RegisterRequest req) {
    String u = req.getUsername().trim();
    String phone = req.getPhone().trim();
    if (sysUserRepository.existsByUsernameOrPhone(u, phone)) {
      throw new EntityExistsException("用户名或手机号已被占用");
    }
    SysUser user = new SysUser();
    user.setUsername(u);
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setName(req.getName().trim());
    user.setPhone(phone);
    user.setRole(Role.PATIENT);
    sysUserRepository.save(user);

    Patient p = new Patient();
    p.setLinkedUser(user);
    p.setName(req.getName().trim());
    p.setGender(req.getGender());
    p.setBirthDate(req.getBirthDate());
    if (req.getBirthDate() != null) {
      int y = java.time.LocalDate.now().getYear() - req.getBirthDate().getYear();
      p.setAge(Math.max(y, 0));
    }
    p.setPhone(phone);
    p.setIdCard(req.getIdCard().trim());
    patientRepository.save(p);

    String token = tokenStore.createToken(user.getId());
    return new LoginResponse(token, user.getUsername(), user.getName(), Role.PATIENT, p.getId(), null);
  }
}
