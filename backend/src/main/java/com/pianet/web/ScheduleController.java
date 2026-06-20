package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.ScheduleSlotRequest;
import com.pianet.entity.Schedule;
import com.pianet.service.ScheduleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

  private final ScheduleService scheduleService;

  @GetMapping
  public List<Schedule> upcoming(
      @RequestParam(required = false) LocalDate from,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return scheduleService.upcoming(from, uid);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Schedule create(
      @Valid @RequestBody ScheduleSlotRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return scheduleService.createSlot(body, uid);
  }
}
