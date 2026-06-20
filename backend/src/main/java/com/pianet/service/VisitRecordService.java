package com.pianet.service;

import com.pianet.dto.VisitRecordRequest;
import com.pianet.entity.DoctorProfile;
import com.pianet.entity.Patient;
import com.pianet.entity.Registration;
import com.pianet.entity.SysUser;
import com.pianet.entity.VisitRecord;
import com.pianet.model.Role;
import com.pianet.repository.DoctorProfileRepository;
import com.pianet.repository.PatientRepository;
import com.pianet.repository.RegistrationRepository;
import com.pianet.repository.VisitRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VisitRecordService {

  private final VisitRecordRepository visitRecordRepository;
  private final PatientRepository patientRepository;
  private final DoctorProfileRepository doctorProfileRepository;
  private final RegistrationRepository registrationRepository;
  private final AccessService accessService;

  @Transactional(readOnly = true)
  public Page<VisitRecord> list(Long patientId, Long currentUserId, Pageable pageable) {
    SysUser u = accessService.requireUser(currentUserId);
    if (u.getRole() == Role.PATIENT) {
      Patient mine =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
      if (patientId != null && !patientId.equals(mine.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能查看其他病人的看诊记录");
      }
      return visitRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(mine.getId(), pageable);
    }
    if (patientId == null) {
      return visitRecordRepository.findAllByOrderByVisitDateDescCreatedAtDesc(pageable);
    }
    return visitRecordRepository.findByPatientIdOrderByVisitDateDescCreatedAtDesc(patientId, pageable);
  }

  @Transactional(readOnly = true)
  public VisitRecord get(Long id, Long currentUserId) {
    VisitRecord v =
        visitRecordRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("看诊记录不存在: " + id));
    assertPatientAccess(v.getPatient().getId(), currentUserId);
    return v;
  }

  private void assertPatientAccess(Long patientId, Long userId) {
    SysUser u = accessService.requireUser(userId);
    if (u.getRole() == Role.PATIENT) {
      Patient mine =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
      if (!mine.getId().equals(patientId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问");
      }
    }
  }

  @Transactional
  public VisitRecord create(VisitRecordRequest req, Long currentUserId) {
    accessService.requireDoctorOrAdmin(currentUserId);
    Patient patient =
        patientRepository
            .findById(req.getPatientId())
            .orElseThrow(() -> new EntityNotFoundException("病人不存在: " + req.getPatientId()));
    VisitRecord v = new VisitRecord();
    v.setPatient(patient);
    apply(v, req);
    return visitRecordRepository.save(v);
  }

  @Transactional
  public VisitRecord update(Long id, VisitRecordRequest req, Long currentUserId) {
    accessService.requireDoctorOrAdmin(currentUserId);
    VisitRecord v =
        visitRecordRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("看诊记录不存在: " + id));
    Patient patient =
        patientRepository
            .findById(req.getPatientId())
            .orElseThrow(() -> new EntityNotFoundException("病人不存在: " + req.getPatientId()));
    v.setPatient(patient);
    apply(v, req);
    return visitRecordRepository.save(v);
  }

  @Transactional
  public void delete(Long id, Long currentUserId) {
    accessService.requireDoctorOrAdmin(currentUserId);
    if (!visitRecordRepository.existsById(id)) {
      throw new EntityNotFoundException("看诊记录不存在: " + id);
    }
    visitRecordRepository.deleteById(id);
  }

  private void apply(VisitRecord v, VisitRecordRequest req) {
    v.setVisitDate(req.getVisitDate());
    v.setDepartment(req.getDepartment());
    v.setDoctorName(req.getDoctorName());
    String sym = req.getSymptoms() != null ? req.getSymptoms() : req.getChiefComplaint();
    v.setSymptoms(sym);
    v.setPhysicalSigns(req.getPhysicalSigns());
    v.setDiagnosis(req.getDiagnosis());
    v.setPrescription(req.getPrescription());
    v.setAdvice(req.getAdvice());
    v.setNotes(req.getNotes());
    if (req.getVisitStatus() != null && !req.getVisitStatus().isBlank()) {
      v.setVisitStatus(req.getVisitStatus());
    } else if (v.getVisitStatus() == null) {
      v.setVisitStatus("已完成");
    }
    if (req.getDoctorProfileId() != null) {
      DoctorProfile dp =
          doctorProfileRepository
              .findById(req.getDoctorProfileId())
              .orElseThrow(
                  () -> new EntityNotFoundException("医生档案不存在: " + req.getDoctorProfileId()));
      v.setDoctorProfile(dp);
      if (req.getDoctorName() == null || req.getDoctorName().isBlank()) {
        v.setDoctorName(dp.getUser().getName());
      }
      if (dp.getDepartment() != null
          && (req.getDepartment() == null || req.getDepartment().isBlank())) {
        v.setDepartment(dp.getDepartment().getName());
      }
    }
    if (req.getRegistrationId() != null) {
      Registration reg =
          registrationRepository
              .findById(req.getRegistrationId())
              .orElseThrow(
                  () -> new EntityNotFoundException("挂号记录不存在: " + req.getRegistrationId()));
      v.setRegistration(reg);
    }
  }
}
