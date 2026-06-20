package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.PageResult;
import com.pianet.dto.PatientDetailResponse;
import com.pianet.dto.PatientRequest;
import com.pianet.entity.Patient;
import com.pianet.service.PatientService;
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
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

  private final PatientService patientService;

  @GetMapping
  public PageResult<Patient> list(
      @RequestParam(defaultValue = "false") boolean includeArchived,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    Page<Patient> p = patientService.list(includeArchived, keyword, userId, PageRequest.of(page, size));
    return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }

  @GetMapping("/{id}")
  public PatientDetailResponse get(
      @PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return patientService.getDetail(id, userId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Patient create(
      @Valid @RequestBody PatientRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return patientService.create(body, userId);
  }

  @PutMapping("/{id}")
  public Patient update(
      @PathVariable Long id,
      @Valid @RequestBody PatientRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    return patientService.update(id, body, userId);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long userId) {
    patientService.delete(id, userId);
  }
}
