package com.pianet.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResult<T> {

  private List<T> content;
  private int page;
  private int size;
  private long totalElements;
  private int totalPages;
}
