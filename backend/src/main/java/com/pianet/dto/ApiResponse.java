package com.pianet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

  private int code;
  private String message;
  private T data;
  private long timestamp;
}
