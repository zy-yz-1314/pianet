package com.pianet.web;

import com.pianet.auth.AuthInterceptor;
import com.pianet.dto.DepartmentRequest;
import com.pianet.entity.Department;
import com.pianet.service.DepartmentService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

  private final DepartmentService departmentService;

  @GetMapping
  public List<Department> list() {
    return departmentService.listAll();
  }

  @GetMapping("/{id}")
  public Department get(@PathVariable Long id) {
    return departmentService.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Department create(
      @Valid @RequestBody DepartmentRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return departmentService.create(body, uid);
  }

  @PutMapping("/{id}")
  public Department update(
      @PathVariable Long id,
      @Valid @RequestBody DepartmentRequest body,
      @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    return departmentService.update(id, body, uid);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @RequestAttribute(AuthInterceptor.ATTR_USER_ID) Long uid) {
    departmentService.delete(id, uid);
  }
}
