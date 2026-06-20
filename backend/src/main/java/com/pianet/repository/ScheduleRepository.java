package com.pianet.repository;

import com.pianet.entity.Schedule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

  List<Schedule> findByWorkDateGreaterThanEqualOrderByWorkDateAscStartTimeAsc(LocalDate from);

  List<Schedule> findByDoctorProfileIdAndWorkDate(Long doctorProfileId, LocalDate date);
}
