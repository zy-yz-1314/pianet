-- PCMS 病人看诊管理系统 — MySQL 初始化脚本
-- 在 MySQL 中执行：CREATE DATABASE 后 SOURCE 本文件，或复制语句执行
-- 注意：Spring Boot JPA ddl-auto:update 可自动建表，本脚本提供完整的手工建表参考

CREATE DATABASE IF NOT EXISTS pianet
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE pianet;

-- ============================================================
-- 基础表
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  introduction TEXT,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_department_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS medicines (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  specification VARCHAR(100),
  usage_text VARCHAR(200),
  taboo TEXT,
  stock INT DEFAULT 0,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_medicine_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 用户与角色
-- ============================================================

CREATE TABLE IF NOT EXISTS sys_users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  name VARCHAR(50),
  phone VARCHAR(20),
  role ENUM('ADMIN','DOCTOR','PATIENT') NOT NULL DEFAULT 'PATIENT',
  enabled BIT(1) DEFAULT 1,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 病人档案
-- ============================================================

CREATE TABLE IF NOT EXISTS patients (
  id BIGINT NOT NULL AUTO_INCREMENT,
  linked_user_id BIGINT,
  department_id BIGINT,
  name VARCHAR(64) NOT NULL,
  gender VARCHAR(8),
  birth_date DATE,
  age INT,
  phone VARCHAR(32),
  id_card VARCHAR(32),
  address VARCHAR(200),
  allergy_history TEXT,
  past_medical_history TEXT,
  family_medical_history TEXT,
  medication_history TEXT,
  archived BIT(1) DEFAULT 0,
  PRIMARY KEY (id),
  INDEX idx_patient_name (name),
  INDEX idx_patient_archived (archived),
  CONSTRAINT fk_patient_user FOREIGN KEY (linked_user_id) REFERENCES sys_users (id) ON DELETE SET NULL,
  CONSTRAINT fk_patient_dept FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 医生档案
-- ============================================================

CREATE TABLE IF NOT EXISTS doctor_profiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  department_id BIGINT NOT NULL,
  title VARCHAR(50),
  introduction TEXT,
  work_years INT,
  PRIMARY KEY (id),
  UNIQUE KEY uk_doctor_user (user_id),
  CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES sys_users (id) ON DELETE CASCADE,
  CONSTRAINT fk_doctor_dept FOREIGN KEY (department_id) REFERENCES departments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 排班
-- ============================================================

CREATE TABLE IF NOT EXISTS schedules (
  id BIGINT NOT NULL AUTO_INCREMENT,
  doctor_profile_id BIGINT NOT NULL,
  work_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  max_num INT DEFAULT 10,
  current_num INT DEFAULT 0,
  created_at DATETIME(6),
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  INDEX idx_schedule_date (work_date),
  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 挂号
-- ============================================================

CREATE TABLE IF NOT EXISTS registrations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_profile_id BIGINT NOT NULL,
  schedule_id BIGINT NOT NULL,
  registration_no VARCHAR(50),
  status VARCHAR(20) DEFAULT '待就诊',
  queue_no INT,
  registration_time DATETIME(6),
  cancel_time DATETIME(6),
  visit_time DATETIME(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_reg_no (registration_no),
  INDEX idx_reg_patient (patient_id),
  INDEX idx_reg_status (status),
  CONSTRAINT fk_reg_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
  CONSTRAINT fk_reg_doctor FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profiles (id),
  CONSTRAINT fk_reg_schedule FOREIGN KEY (schedule_id) REFERENCES schedules (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 看诊记录
-- ============================================================

CREATE TABLE IF NOT EXISTS visit_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_profile_id BIGINT,
  registration_id BIGINT,
  visit_date DATE,
  department VARCHAR(64),
  doctor_name VARCHAR(64),
  chief_complaint VARCHAR(512),
  symptoms TEXT,
  physical_signs VARCHAR(200),
  diagnosis TEXT,
  prescription TEXT,
  advice TEXT,
  notes VARCHAR(1024),
  visit_status VARCHAR(20) DEFAULT '已完成',
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6),
  PRIMARY KEY (id),
  INDEX idx_visit_patient (patient_id),
  INDEX idx_visit_date (visit_date),
  CONSTRAINT fk_visit_patient FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE CASCADE,
  CONSTRAINT fk_visit_doctor FOREIGN KEY (doctor_profile_id) REFERENCES doctor_profiles (id) ON DELETE SET NULL,
  CONSTRAINT fk_visit_reg FOREIGN KEY (registration_id) REFERENCES registrations (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- AI 咨询记录
-- ============================================================

CREATE TABLE IF NOT EXISTS ai_chats (
  id BIGINT NOT NULL AUTO_INCREMENT,
  patient_id BIGINT NOT NULL,
  doctor_viewer_id BIGINT,
  question TEXT NOT NULL,
  answer TEXT NOT NULL,
  chat_time DATETIME(6),
  PRIMARY KEY (id),
  INDEX idx_aichat_patient (patient_id),
  CONSTRAINT fk_aichat_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
  CONSTRAINT fk_aichat_doctor FOREIGN KEY (doctor_viewer_id) REFERENCES doctor_profiles (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 操作日志
-- ============================================================

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  operation VARCHAR(100),
  operation_time DATETIME(6),
  ip_address VARCHAR(50),
  remark VARCHAR(200),
  PRIMARY KEY (id),
  INDEX idx_oplog_user (user_id),
  INDEX idx_oplog_time (operation_time),
  CONSTRAINT fk_oplog_user FOREIGN KEY (user_id) REFERENCES sys_users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
