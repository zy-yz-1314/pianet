package com.pianet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PianetApplication {

  public static void main(String[] args) {
    SpringApplication.run(PianetApplication.class, args);
  }
}
