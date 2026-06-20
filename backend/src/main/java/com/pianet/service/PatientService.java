package com.pianet.service;

import com.pianet.dto.PatientDetailResponse;
import com.pianet.dto.PatientRequest;
import com.pianet.entity.Department;
import com.pianet.entity.Patient;
import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import com.pianet.repository.DepartmentRepository;
import com.pianet.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PatientService {

  private final PatientRepository patientRepository;
  private final DepartmentRepository departmentRepository;
  private final AccessService accessService;

  @Transactional(readOnly = true)
  public Page<Patient> list(boolean includeArchived, String keyword, Long currentUserId, Pageable pageable) {
    SysUser u = accessService.requireUser(currentUserId);
    if (u.getRole() == Role.PATIENT) {
      return patientRepository
          .findByLinkedUser_Id(u.getId())
          .map(p -> (Page<Patient>) new org.springframework.data.domain.PageImpl<>(
              Collections.singletonList(p), pageable, 1))
          .orElse(Page.empty());
    }
    boolean hasKw = keyword != null && !keyword.isBlank();
    if (hasKw) {
      if (includeArchived) {
        return patientRepository.searchByKeyword(keyword.trim(), pageable);
      }
      return patientRepository.searchByKeywordExcludeArchived(keyword.trim(), pageable);
    }
    if (includeArchived) {
      return patientRepository.findAllByOrderByIdDesc(pageable);
    }
    return patientRepository.findByArchivedFalseOrderByIdDesc(pageable);
  }

  private void assertCanAccessPatient(Long patientId, Long currentUserId) {
    SysUser u = accessService.requireUser(currentUserId);
    if (u.getRole() == Role.PATIENT) {
      Patient mine =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
      if (!mine.getId().equals(patientId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能查看其他病人信息");
      }
    }
  }

  @Transactional(readOnly = true)
  public PatientDetailResponse getDetail(Long id, Long currentUserId) {
    assertCanAccessPatient(id, currentUserId);
    Patient p =
        patientRepository
            .findByIdWithVisits(id)
            .orElseThrow(() -> new EntityNotFoundException("病人不存在: " + id));
    Long deptId = p.getDepartment() != null ? p.getDepartment().getId() : null;
    String deptName = p.getDepartment() != null ? p.getDepartment().getName() : null;
    return new PatientDetailResponse(
        p.getId(),
        deptId,
        deptName,
        p.getName(),
        p.getGender(),
        p.getBirthDate(),
        p.getAge(),
        p.getPhone(),
        p.getIdCard(),
        p.getAddress(),
        p.getAllergyHistory(),
        p.getPastMedicalHistory(),
        p.getFamilyMedicalHistory(),
        p.getMedicationHistory(),
        p.isArchived(),
        p.getVisits());
  }

  @Transactional
  public Patient create(PatientRequest req, Long currentUserId) {
    accessService.requireDoctorOrAdmin(currentUserId);
    Patient p = new Patient();
    apply(p, req);
    return patientRepository.save(p);
  }

  @Transactional
  public Patient update(Long id, PatientRequest req, Long currentUserId) {
    accessService.requireDoctorOrAdmin(currentUserId);
    Patient p =
        patientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("病人不存在: " + id));
    apply(p, req);
    return patientRepository.save(p);
  }

  @Transactional
  public void delete(Long id, Long currentUserId) {
    accessService.requireAdmin(currentUserId);
    if (!patientRepository.existsById(id)) {
      throw new EntityNotFoundException("病人不存在: " + id);
    }
    patientRepository.deleteById(id);
  }

  private void apply(Patient p, PatientRequest req) {
    p.setName(req.getName());
    p.setGender(req.getGender());
    p.setBirthDate(req.getBirthDate());
    if (req.getAge() != null) {
      p.setAge(req.getAge());
    } else if (req.getBirthDate() != null) {
      int y = java.time.LocalDate.now().getYear() - req.getBirthDate().getYear();
      p.setAge(Math.max(y, 0));
    }
    p.setPhone(req.getPhone());
    p.setIdCard(req.getIdCard());
    p.setAddress(req.getAddress());
    p.setAllergyHistory(req.getAllergyHistory());
    p.setPastMedicalHistory(req.getPastMedicalHistory());
    p.setFamilyMedicalHistory(req.getFamilyMedicalHistory());
    p.setMedicationHistory(req.getMedicationHistory());
    if (req.getArchived() != null) {
      p.setArchived(req.getArchived());
    }
    if (req.getDepartmentId() != null) {
      Department d =
          departmentRepository
              .findById(req.getDepartmentId())
              .orElseThrow(() -> new EntityNotFoundException("科室不存在: " + req.getDepartmentId()));
      p.setDepartment(d);
    }
  }
}
