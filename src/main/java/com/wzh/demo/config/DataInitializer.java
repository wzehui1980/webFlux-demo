package com.wzh.demo.config;

import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.RoleRepository;
import com.wzh.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * DataInitializer类实现CommandLineRunner接口，用于在应用程序启动时初始化基础数据
 * 该类使用Spring框架的注解和依赖注入功能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  // 注入UserRepository，用于用户数据的操作
  private final UserRepository userRepository;
  // 注入RoleRepository，用于角色数据的操作
  private final RoleRepository roleRepository;

  /**
   * 实现CommandLineRunner接口的run方法，在应用启动时执行
   * 
   * @param args 命令行参数
   * @throws Exception 可能抛出的异常
   */
  @Override
  public void run(String... args) throws Exception {
    // 检查是否已有数据
    if (roleRepository.count() == 0) {
      // 创建初始角色
      RoleEntity adminRole = new RoleEntity(null, "ADMIN", "系统管理员，拥有所有权限");
      RoleEntity userRole = new RoleEntity(null, "USER", "普通用户，拥有基本权限");
      RoleEntity managerRole = new RoleEntity(null, "MANAGER", "经理，拥有管理权限");

      roleRepository.save(adminRole);
      roleRepository.save(userRole);
      roleRepository.save(managerRole);

      log.info("初始角色已创建: ADMIN, USER, MANAGER");
    }

    if (userRepository.count() == 0) {
      // 获取角色
      RoleEntity adminRole = roleRepository.findByName("ADMIN").orElse(null);
      RoleEntity userRole = roleRepository.findByName("USER").orElse(null);

      // 创建管理员用户
      Set<RoleEntity> adminRoles = new HashSet<>();
      adminRoles.add(adminRole);
      UserEntity admin = new UserEntity();
      admin.setName("admin");
      admin.setAge(30);
      admin.setPhone("13800000000");
      admin.setEmail("admin@example.com");
      admin.setActive(true);
      admin.setRoles(adminRoles);

      // 创建普通用户
      Set<RoleEntity> userRoles = new HashSet<>();
      userRoles.add(userRole);

      UserEntity user1 = new UserEntity();
      user1.setName("张三");
      user1.setAge(20);
      user1.setPhone("13800138000");
      user1.setEmail("zhangsan@example.com");
      user1.setActive(true);
      user1.setRoles(userRoles);

      UserEntity user2 = new UserEntity();
      user2.setName("李四");
      user2.setAge(22);
      user2.setPhone("13800138001");
      user2.setEmail("lisi@example.com");
      user2.setActive(true);
      user2.setRoles(userRoles);

      UserEntity user3 = new UserEntity();
      user3.setName("王五");
      user3.setAge(25);
      user3.setPhone("13800138002");
      user3.setEmail("wangwu@example.com");
      user3.setActive(true);
      user3.setRoles(userRoles);

      userRepository.save(admin);
      userRepository.save(user1);
      userRepository.save(user2);
      userRepository.save(user3);

      log.info("初始数据已插入");
      log.info("管理员账号: admin / 任意密码");
      log.info("普通用户: 张三, 李四, 王五 / 任意密码");
    }
  }
}
