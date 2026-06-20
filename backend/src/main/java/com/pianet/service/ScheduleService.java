package com.pianet.service;

import com.pianet.dto.ScheduleSlotRequest;
import com.pianet.entity.DoctorProfile;
import com.pianet.entity.Schedule;
import com.pianet.entity.SysUser;
import com.pianet.model.Role;
import com.pianet.repository.DoctorProfileRepository;
import com.pianet.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final DoctorProfileRepository doctorProfileRepository;
  private final AccessService accessService;

  @Transactional(readOnly = true)
  public List<Schedule> upcoming(LocalDate from, Long viewerId) {
    accessService.requireUser(viewerId);
    LocalDate d = from != null ? from : LocalDate.now();
    return scheduleRepository.findByWorkDateGreaterThanEqualOrderByWorkDateAscStartTimeAsc(d);
  }

  @Transactional
  public Schedule createSlot(ScheduleSlotRequest req, Long uid) {
    SysUser u = accessService.requireUser(uid);
    DoctorProfile dp;
    if (u.getRole() == Role.ADMIN) {
      Long id = req.getDoctorProfileId();
      if (id == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "管理员排班请指定 doctorProfileId");
      }
      dp =
          doctorProfileRepository
              .findById(id)
              .orElseThrow(() -> new EntityNotFoundException("医生档案不存在"));
    } else if (u.getRole() == Role.DOCTOR) {
      dp =
          doctorProfileRepository
              .findByUser_Id(uid)
              .orElseThrow(() -> new EntityNotFoundException("当前账号未绑定医生档案"));
    } else {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权创建排班");
    }
    Schedule s = new Schedule();
    s.setDoctorProfile(dp);
    s.setWorkDate(req.getWorkDate());
    s.setStartTime(req.getStartTime());
    s.setEndTime(req.getEndTime());
    int max = req.getMaxNum() != null ? req.getMaxNum() : 10;
    s.setMaxNum(max);
    s.setCurrentNum(0);
    return scheduleRepository.save(s);
  }
}
