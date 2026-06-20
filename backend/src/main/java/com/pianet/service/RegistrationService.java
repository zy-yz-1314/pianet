package com.pianet.service;

import com.pianet.dto.RegistrationBookRequest;
import com.pianet.entity.DoctorProfile;
import com.pianet.entity.Patient;
import com.pianet.entity.Registration;
import com.pianet.entity.Schedule;
import com.pianet.entity.SysUser;
import com.pianet.model.RegistrationStatus;
import com.pianet.model.Role;
import com.pianet.repository.DoctorProfileRepository;
import com.pianet.repository.PatientRepository;
import com.pianet.repository.RegistrationRepository;
import com.pianet.repository.ScheduleRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RegistrationService {

  private final RegistrationRepository registrationRepository;
  private final ScheduleRepository scheduleRepository;
  private final PatientRepository patientRepository;
  private final DoctorProfileRepository doctorProfileRepository;
  private final AccessService accessService;

  private void ensurePatientBooking(Long patientId, Long uid) {
    SysUser u = accessService.requireUser(uid);
    if (u.getRole() == Role.ADMIN || u.getRole() == Role.DOCTOR) {
      return;
    }
    if (u.getRole() == Role.PATIENT) {
      Patient p =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
      if (!p.getId().equals(patientId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能为自己挂号");
      }
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权挂号");
  }

  @Transactional
  public Registration book(RegistrationBookRequest req, Long uid) {
    ensurePatientBooking(req.getPatientId(), uid);
    Patient patient =
        patientRepository
            .findById(req.getPatientId())
            .orElseThrow(() -> new EntityNotFoundException("病人不存在"));
    Schedule schedule =
        scheduleRepository
            .findById(req.getScheduleId())
            .orElseThrow(() -> new EntityNotFoundException("排班不存在"));
    synchronized (schedule.getId().toString().intern()) {
      Schedule fresh = scheduleRepository.findById(schedule.getId()).orElseThrow();
      if (fresh.getCurrentNum() >= fresh.getMaxNum()) {
        throw new EntityExistsException("该时段号源已满");
      }
      int q = fresh.getCurrentNum() + 1;
      fresh.setCurrentNum(q);
      scheduleRepository.save(fresh);

      Registration r = new Registration();
      r.setPatient(patient);
      r.setDoctorProfile(fresh.getDoctorProfile());
      r.setSchedule(fresh);
      r.setRegistrationNo(
          "GH" + fresh.getWorkDate().toString().replace("-", "") + "-" + q + "-" + patient.getId());
      r.setQueueNo(q);
      r.setStatus(RegistrationStatus.PENDING);
      return registrationRepository.save(r);
    }
  }

  @Transactional(readOnly = true)
  public Page<Registration> listForViewer(Long viewerId, Long doctorProfileFilter, Pageable pageable) {
    SysUser u = accessService.requireUser(viewerId);
    if (u.getRole() == Role.ADMIN) {
      if (doctorProfileFilter != null) {
        return registrationRepository.findByDoctorProfileIdOrderByRegistrationTimeAsc(
            doctorProfileFilter, pageable);
      }
      return registrationRepository.findAllByOrderByRegistrationTimeDesc(pageable);
    }
    if (u.getRole() == Role.DOCTOR) {
      Long dp =
          doctorProfileRepository
              .findByUser_Id(viewerId)
              .map(DoctorProfile::getId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "医生档案缺失"));
      return registrationRepository.findByDoctorProfileIdOrderByRegistrationTimeAsc(dp, pageable);
    }
    Patient p =
        patientRepository
            .findByLinkedUser_Id(u.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
    return registrationRepository.findByPatientIdOrderByRegistrationTimeDesc(p.getId(), pageable);
  }

  @Transactional
  public Registration updateStatus(Long id, String status, Long uid) {
    accessService.requireDoctorOrAdmin(uid);
    Registration r =
        registrationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("挂号不存在"));
    SysUser u = accessService.requireUser(uid);
    if (u.getRole() == Role.DOCTOR) {
      Long myDp =
          doctorProfileRepository
              .findByUser_Id(uid)
              .map(DoctorProfile::getId)
              .orElse(-1L);
      if (!r.getDoctorProfile().getId().equals(myDp)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能修改其他医生的接诊");
      }
    }
    r.setStatus(status);
    if (RegistrationStatus.CANCELLED.equals(status)) {
      r.setCancelTime(LocalDateTime.now());
    }
    if (RegistrationStatus.COMPLETED.equals(status) || RegistrationStatus.IN_PROGRESS.equals(status)) {
      r.setVisitTime(LocalDateTime.now());
    }
    return registrationRepository.save(r);
  }

  @Transactional
  public void cancel(Long id, Long uid) {
    Registration r =
        registrationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("挂号不存在"));
    SysUser u = accessService.requireUser(uid);
    if (u.getRole() == Role.PATIENT) {
      Patient p =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无档案绑定"));
      if (!r.getPatient().getId().equals(p.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权取消");
      }
    } else if (u.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权取消");
    }
    if (!RegistrationStatus.PENDING.equals(r.getStatus())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可取消");
    }
    r.setStatus(RegistrationStatus.CANCELLED);
    r.setCancelTime(LocalDateTime.now());
    Schedule sc = r.getSchedule();
    sc.setCurrentNum(Math.max(0, sc.getCurrentNum() - 1));
    scheduleRepository.save(sc);
    registrationRepository.save(r);
  }
}
