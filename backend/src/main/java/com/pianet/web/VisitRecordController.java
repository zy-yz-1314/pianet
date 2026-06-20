package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.PageResult;
import com.pianet.dto.VisitRecordRequest;
import com.pianet.entity.VisitRecord;
import com.pianet.service.VisitRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitRecordController {

  private final VisitRecordService visitRecordService;

  @GetMapping
  public PageResult<VisitRecord> list(
      @RequestParam(required = false) Long patientId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    Page<VisitRecord> p = visitRecordService.list(patientId, userId, PageRequest.of(page, size));
    return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }

  @GetMapping("/{id}")
  public VisitRecord get(
      @PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return visitRecordService.get(id, userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public VisitRecord create(
      @Valid @RequestBody VisitRecordRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return visitRecordService.create(body, userId);
  }

  @PutMapping("/{id}")
  public VisitRecord update(
      @PathVariable Long id,
      @Valid @RequestBody VisitRecordRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return visitRecordService.update(id, body, userId);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    visitRecordService.delete(id, userId);
  }
}
