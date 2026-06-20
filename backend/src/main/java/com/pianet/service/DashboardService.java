package com.pianet.service;

import com.pianet.entity.DoctorProfile;
import com.pianet.entity.Patient;
import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import com.pianet.repository.AiChatRepository;
import com.pianet.repository.DepartmentRepository;
import com.pianet.repository.DoctorProfileRepository;
import com.pianet.repository.MedicineRepository;
import com.pianet.repository.PatientRepository;
import com.pianet.repository.RegistrationRepository;
import com.pianet.repository.VisitRecordRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final AccessService accessService;
  private final PatientRepository patientRepository;
  private final VisitRecordRepository visitRecordRepository;
  private final RegistrationRepository registrationRepository;
  private final DepartmentRepository departmentRepository;
  private final MedicineRepository medicineRepository;
  private final DoctorProfileRepository doctorProfileRepository;
  private final AiChatRepository aiChatRepository;

  public Map<String, Object> overview(Long userId) {
    SysUser u = accessService.requireUser(userId);
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("systemName", "病人看诊管理系统（PCMS）");
    m.put("role", u.getRole().name());
    m.put("displayName", u.getName());
    LocalDate today = LocalDate.now();
    if (u.getRole() == Role.ADMIN) {
      m.put("patients", patientRepository.count());
      m.put("visitRecords", visitRecordRepository.count());
      m.put("registrations", registrationRepository.count());
      m.put("departments", departmentRepository.count());
      m.put("medicines", medicineRepository.count());
      m.put("todayVisits", visitRecordRepository.countByVisitDate(today));
      m.put("pendingRegistrationsTotal", registrationRepository.countByStatus("待就诊"));
    } else if (u.getRole() == Role.DOCTOR) {
      DoctorProfile dp =
          doctorProfileRepository
              .findByUser_Id(userId)
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "医生档案未配置"));
      m.put("doctorTitle", dp.getTitle());
      m.put("department", dp.getDepartment().getName());
      m.put("pendingRegistrations",
          registrationRepository.countByDoctorProfileIdAndStatus(dp.getId(), "待就诊"));
      m.put("todayVisits", visitRecordRepository.countByVisitDate(today));
      m.put("myVisitRecordsTotal", visitRecordRepository.countByDoctorProfile_Id(dp.getId()));
    } else {
      Patient p =
          patientRepository
              .findByLinkedUser_Id(u.getId())
              .orElse(null);
      if (p != null) {
        m.put("patientId", p.getId());
        m.put(
            "myPendingRegistrations",
            registrationRepository.findByPatientIdOrderByRegistrationTimeDesc(p.getId(), PageRequest.of(0, 100)).stream()
                .filter(r -> "待就诊".equals(r.getStatus()))
                .count());
        m.put(
            "myAiConsults",
            aiChatRepository.findByPatientIdOrderByChatTimeDesc(p.getId()).size());
      }
    }
    return clean(m);
  }

  /** 占位：全院待接诊需单独查询所有医生聚合，此处用全表 status 计数 */
  private Map<String, Object> clean(Map<String, Object> m) {
    m.entrySet().removeIf(e -> e.getValue() == null);
    return m;
  }

  public Map<String, Object> charts(Long userId) {
    SysUser u = accessService.requireUser(userId);
    Map<String, Object> m = new LinkedHashMap<>();
    LocalDate today = LocalDate.now();

    // 近 7 天就诊趋势
    java.util.List<Map<String, Object>> trend = new java.util.ArrayList<>();
    for (int i = 6; i >= 0; i--) {
      LocalDate d = today.minusDays(i);
      long count = visitRecordRepository.countByVisitDate(d);
      trend.add(Map.of("date", d.toString(), "count", count));
    }
    m.put("visitTrend", trend);

    // 挂号状态分布
    long pending = registrationRepository.countByStatus("待就诊");
    long inProgress = registrationRepository.countByStatus("就诊中");
    long completed = registrationRepository.countByStatus("已完成");
    long cancelled = registrationRepository.countByStatus("已取消");
    m.put("registrationStatus", java.util.List.of(
      Map.of("name", "待就诊", "value", pending),
      Map.of("name", "就诊中", "value", inProgress),
      Map.of("name", "已完成", "value", completed),
      Map.of("name", "已取消", "value", cancelled)
    ));

    return m;
  }
}
