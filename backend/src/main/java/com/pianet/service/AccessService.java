package com.pianet.service;

import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import com.pianet.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccessService {

  private final SysUserRepository sysUserRepository;

  public SysUser requireUser(Long userId) {
    return sysUserRepository
        .findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录"));
  }

  public void requireAdmin(Long userId) {
    if (requireUser(userId).getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要管理员权限");
    }
  }

  public void requireDoctorOrAdmin(Long userId) {
    Role r = requireUser(userId).getRole();
    if (r != Role.DOCTOR && r != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要医生或管理员权限");
    }
  }

  public void requirePatient(Long userId) {
    if (requireUser(userId).getRole() != Role.PATIENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要病人账号");
    }
  }
}
