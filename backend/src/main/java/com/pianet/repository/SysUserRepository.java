package com.pianet.repository;

import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysUserRepository extends JpaRepository<SysUser, Long> {

  Optional<SysUser> findByUsername(String username);

  boolean existsByUsernameOrPhone(String username, String phone);

  long countByRole(Role role);
}
