package com.pianet.service;

import com.pianet.dto.DepartmentRequest;
import com.pianet.entity.Department;
import com.pianet.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final AccessService accessService;

  public List<Department> listAll() {
    return departmentRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Department get(Long id) {
    return departmentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("科室不存在"));
  }

  @Transactional
  public Department create(DepartmentRequest req, Long uid) {
    accessService.requireAdmin(uid);
    Department d = new Department();
    d.setName(req.getName().trim());
    d.setIntroduction(req.getIntroduction());
    return departmentRepository.save(d);
  }

  @Transactional
  public Department update(Long id, DepartmentRequest req, Long uid) {
    accessService.requireAdmin(uid);
    Department d = get(id);
    d.setName(req.getName().trim());
    d.setIntroduction(req.getIntroduction());
    return departmentRepository.save(d);
  }

  @Transactional
  public void delete(Long id, Long uid) {
    accessService.requireAdmin(uid);
    departmentRepository.deleteById(id);
  }
}
