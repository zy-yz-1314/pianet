package com.pianet.bootstrap;

import com.pianet.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 数据初始化：仅首次启动时执行。当前种子数据已入库，此组件保留用于未来重置场景。 */
@Component
@Order(1)
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final SysUserRepository sysUserRepository;

  @Override
  public void run(String... args) {
    if (sysUserRepository.count() > 0) {
      return; // 已有数据，跳过
    }
    // 如需重新播种，在此添加初始化逻辑
  }
}
