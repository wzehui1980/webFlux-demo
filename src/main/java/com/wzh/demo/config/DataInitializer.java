package com.wzh.demo.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 数据初始化器 - 使用 Java 代码插入中文数据，避免 Windows 环境下 SQL 文件编码问题
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

  private final DatabaseClient databaseClient;

  public DataInitializer(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
  }

  @Override
  public void run(String... args) {
    log.info("=== 开始初始化测试数据 ===");

    // 使用 block() 确保按顺序执行，避免异步问题
    try {
      clearExistingData().block();
      log.info("正在插入角色数据...");
      insertRoles().block();
      log.info("角色数据插入成功");

      log.info("正在插入用户数据...");
      insertUsers().block();
      log.info("用户数据插入成功");

      log.info("正在插入用户角色关系...");
      insertUserRoles().block();
      log.info("用户角色关系插入成功");

      log.info("=== 测试数据初始化完成 ===");
    } catch (Exception e) {
      log.error("数据初始化失败", e);
    }
  }

  private Mono<Void> clearExistingData() {
    log.info("正在清空现有数据...");
    return databaseClient.sql("SET REFERENTIAL_INTEGRITY FALSE")
        .fetch()
        .rowsUpdated()
        .then(databaseClient.sql("TRUNCATE TABLE user_roles").fetch().rowsUpdated())
        .then(databaseClient.sql("TRUNCATE TABLE users").fetch().rowsUpdated())
        .then(databaseClient.sql("TRUNCATE TABLE roles").fetch().rowsUpdated())
        .then(databaseClient.sql("SET REFERENTIAL_INTEGRITY TRUE").fetch().rowsUpdated())
        .doOnSuccess(v -> log.info("已清空现有数据"))
        .then();
  }

  private Mono<Void> insertRoles() {
    return databaseClient.sql(
        "INSERT INTO roles (name, description, create_time, update_time) VALUES " +
            "(:name, :description, :createTime, :updateTime)")
        .bind("name", "ADMIN")
        .bind("description", "系统管理员，拥有所有权限")
        .bind("createTime", LocalDateTime.now())
        .bind("updateTime", LocalDateTime.now())
        .fetch()
        .rowsUpdated()
        .then(
            databaseClient.sql(
                "INSERT INTO roles (name, description, create_time, update_time) VALUES " +
                    "(:name, :description, :createTime, :updateTime)")
                .bind("name", "USER")
                .bind("description", "普通用户，拥有基本权限")
                .bind("createTime", LocalDateTime.now())
                .bind("updateTime", LocalDateTime.now())
                .fetch()
                .rowsUpdated())
        .then(
            databaseClient.sql(
                "INSERT INTO roles (name, description, create_time, update_time) VALUES " +
                    "(:name, :description, :createTime, :updateTime)")
                .bind("name", "MANAGER")
                .bind("description", "经理，拥有管理权限")
                .bind("createTime", LocalDateTime.now())
                .bind("updateTime", LocalDateTime.now())
                .fetch()
                .rowsUpdated())
        .then();
  }

  private Mono<Void> insertUsers() {
    return databaseClient.sql(
        "INSERT INTO users (name, age, phone, email, active, create_time, update_time) VALUES " +
            "(:name, :age, :phone, :email, :active, :createTime, :updateTime)")
        .bind("name", "admin")
        .bind("age", 30)
        .bind("phone", "13800000000")
        .bind("email", "admin@example.com")
        .bind("active", true)
        .bind("createTime", LocalDateTime.now())
        .bind("updateTime", LocalDateTime.now())
        .fetch()
        .rowsUpdated()
        .then(
            databaseClient.sql(
                "INSERT INTO users (name, age, phone, email, active, create_time, update_time) VALUES " +
                    "(:name, :age, :phone, :email, :active, :createTime, :updateTime)")
                .bind("name", "张三")
                .bind("age", 20)
                .bind("phone", "13800138000")
                .bind("email", "zhangsan@example.com")
                .bind("active", true)
                .bind("createTime", LocalDateTime.now())
                .bind("updateTime", LocalDateTime.now())
                .fetch()
                .rowsUpdated())
        .then(
            databaseClient.sql(
                "INSERT INTO users (name, age, phone, email, active, create_time, update_time) VALUES " +
                    "(:name, :age, :phone, :email, :active, :createTime, :updateTime)")
                .bind("name", "李四")
                .bind("age", 22)
                .bind("phone", "13800138001")
                .bind("email", "lisi@example.com")
                .bind("active", true)
                .bind("createTime", LocalDateTime.now())
                .bind("updateTime", LocalDateTime.now())
                .fetch()
                .rowsUpdated())
        .then(
            databaseClient.sql(
                "INSERT INTO users (name, age, phone, email, active, create_time, update_time) VALUES " +
                    "(:name, :age, :phone, :email, :active, :createTime, :updateTime)")
                .bind("name", "王五")
                .bind("age", 25)
                .bind("phone", "13800138002")
                .bind("email", "wangwu@example.com")
                .bind("active", true)
                .bind("createTime", LocalDateTime.now())
                .bind("updateTime", LocalDateTime.now())
                .fetch()
                .rowsUpdated())
        .then();
  }

  private Mono<Void> insertUserRoles() {
    return databaseClient.sql("INSERT INTO user_roles (user_id, role_id) VALUES (1, 1)")
        .fetch()
        .rowsUpdated()
        .then(databaseClient.sql("INSERT INTO user_roles (user_id, role_id) VALUES (2, 2)")
            .fetch()
            .rowsUpdated())
        .then(databaseClient.sql("INSERT INTO user_roles (user_id, role_id) VALUES (3, 2)")
            .fetch()
            .rowsUpdated())
        .then(databaseClient.sql("INSERT INTO user_roles (user_id, role_id) VALUES (4, 2)")
            .fetch()
            .rowsUpdated())
        .then();
  }
}
