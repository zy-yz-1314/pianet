package com.pianet.model;

/** 挂号状态常量 */
public final class RegistrationStatus {

  public static final String PENDING = "待就诊";
  public static final String IN_PROGRESS = "就诊中";
  public static final String COMPLETED = "已完成";
  public static final String CANCELLED = "已取消";

  private RegistrationStatus() {}
}
