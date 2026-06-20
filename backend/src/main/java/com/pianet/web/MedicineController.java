package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.MedicineRequest;
import com.pianet.entity.Medicine;
import com.pianet.service.MedicineService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

  private final MedicineService medicineService;

  @GetMapping
  public List<Medicine> list(@RequestParam(required = false) String q) {
    return medicineService.list(q);
  }

  @GetMapping("/{id}")
  public Medicine get(@PathVariable Long id) {
    return medicineService.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Medicine create(
      @Valid @RequestBody MedicineRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return medicineService.create(body, uid);
  }

  @PutMapping("/{id}")
  public Medicine update(
      @PathVariable Long id,
      @Valid @RequestBody MedicineRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return medicineService.update(id, body, uid);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    medicineService.delete(id, uid);
  }
}
