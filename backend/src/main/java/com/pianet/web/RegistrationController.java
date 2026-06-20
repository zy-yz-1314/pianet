package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.PageResult;
import com.pianet.dto.RegistrationBookRequest;
import com.pianet.dto.RegistrationStatusRequest;
import com.pianet.entity.Registration;
import com.pianet.service.RegistrationService;
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
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {

  private final RegistrationService registrationService;

  @GetMapping
  public PageResult<Registration> list(
      @RequestParam(required = false) Long doctorProfileId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    Page<Registration> p = registrationService.listForViewer(uid, doctorProfileId, PageRequest.of(page, size));
    return new PageResult<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Registration book(
      @Valid @RequestBody RegistrationBookRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return registrationService.book(body, uid);
  }

  @PutMapping("/{id}/status")
  public Registration status(
      @PathVariable Long id,
      @Valid @RequestBody RegistrationStatusRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return registrationService.updateStatus(id, body.getStatus(), uid);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void cancel(@PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    registrationService.cancel(id, uid);
  }
}
