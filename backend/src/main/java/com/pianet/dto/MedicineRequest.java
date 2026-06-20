package com.pianet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicineRequest {

  @NotBlank private String name;
  @NotBlank private String category;
  @NotBlank private String specification;
  @NotBlank private String usageText;
  @NotBlank private String taboo;
  private Integer stock;
}
