package com.wzh.demo.service.impl;

import com.wzh.demo.exception.UserNotFoundException;
import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.UserRepository;
import com.wzh.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final DatabaseClient databaseClient;

  @Override
  public Flux<UserEntity> findAll() {
    return userRepository.findAll()
        .flatMap(this::loadUserRoles);
  }

  @Override
  public Mono<UserEntity> findById(Long id) {
    return userRepository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException("用户ID=" + id + "不存在")))
        .flatMap(this::loadUserRoles);
  }

  @Override
  public Mono<UserEntity> save(UserEntity user) {
    return userRepository.findByPhone(user.getPhone())
        .flatMap(existingUser -> Mono.<UserEntity>error(new RuntimeException("手机号" + user.getPhone() + "已被注册")))
        .switchIfEmpty(
            userRepository.save(user)
                .flatMap(savedUser -> saveUserRoles(savedUser.getId(), user.getRoles())
                    .thenReturn(savedUser)));
  }

  @Override
  public Mono<UserEntity> update(Long id, UserEntity user) {
    return findById(id)
        .flatMap(existingUser -> {
          // 检查手机号是否被其他用户使用
          if (!existingUser.getPhone().equals(user.getPhone())) {
            return userRepository.findByPhone(user.getPhone())
                .flatMap(phoneUser -> Mono.<UserEntity>error(new RuntimeException("手机号" + user.getPhone() + "已被注册")))
                .switchIfEmpty(Mono.just(existingUser));
          }
          return Mono.just(existingUser);
        })
        .flatMap(existingUser -> {
          log.info("=== 开始更新用户: {} ===", existingUser.getName());

          existingUser.setName(user.getName());
          existingUser.setAge(user.getAge());
          existingUser.setPhone(user.getPhone());
          existingUser.setEmail(user.getEmail());
          existingUser.setActive(user.getActive());

          // 手动设置更新时间
          existingUser.setUpdateTime(LocalDateTime.now());

          return userRepository.save(existingUser)
              .flatMap(savedUser ->
          // 先删除旧的角色关系，再保存新的
          deleteUserRoles(savedUser.getId())
              .then(saveUserRoles(savedUser.getId(), user.getRoles()))
              .thenReturn(savedUser));
        });
  }

  @Override
  public Mono<Void> deleteById(Long id) {
    return findById(id)
        .then(userRepository.deleteById(id));
  }

  @Override
  public Mono<UserEntity> findByPhone(String phone) {
    return userRepository.findByPhone(phone)
        .switchIfEmpty(Mono.error(new UserNotFoundException("手机号=" + phone + "的用户不存在")))
        .flatMap(this::loadUserRoles);
  }

  @Override
  public Mono<UserEntity> toggleUserStatus(Long id) {
    return findById(id)
        .flatMap(user -> {
          user.setActive(!user.getActive());
          user.setUpdateTime(LocalDateTime.now());
          log.info("用户 {} 状态切换为: {}", user.getName(), user.getActive());
          return userRepository.save(user);
        });
  }

  /**
   * 加载用户的角色列表
   */
  private Mono<UserEntity> loadUserRoles(UserEntity user) {
    String sql = "SELECT r.* FROM roles r INNER JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId";

    return databaseClient.sql(sql)
        .bind("userId", user.getId())
        .map((row, metadata) -> {
          RoleEntity role = new RoleEntity();
          role.setId(row.get("id", Long.class));
          role.setName(row.get("name", String.class));
          role.setDescription(row.get("description", String.class));

          // 处理时间字段，兼容不同的数据库驱动
          Object createTimeObj = row.get("create_time");
          if (createTimeObj instanceof LocalDateTime) {
            role.setCreateTime((LocalDateTime) createTimeObj);
          } else if (createTimeObj instanceof java.sql.Timestamp) {
            role.setCreateTime(((java.sql.Timestamp) createTimeObj).toLocalDateTime());
          }

          Object updateTimeObj = row.get("update_time");
          if (updateTimeObj instanceof LocalDateTime) {
            role.setUpdateTime((LocalDateTime) updateTimeObj);
          } else if (updateTimeObj instanceof java.sql.Timestamp) {
            role.setUpdateTime(((java.sql.Timestamp) updateTimeObj).toLocalDateTime());
          }

          return role;
        })
        .all()
        .collectList()
        .doOnNext(roles -> {
          user.setRoles(new HashSet<>(roles));
          log.debug("用户 {} 的角色数量: {}", user.getName(), roles.size());
        })
        .thenReturn(user);
  }

  /**
   * 保存用户角色关系
   */
  private Mono<Void> saveUserRoles(Long userId, java.util.Set<RoleEntity> roles) {
    if (roles == null || roles.isEmpty()) {
      return Mono.empty();
    }

    return Flux.fromIterable(roles)
        .flatMap(role -> {
          String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)";
          return databaseClient.sql(sql)
              .bind("userId", userId)
              .bind("roleId", role.getId())
              .fetch()
              .rowsUpdated();
        })
        .then();
  }

  /**
   * 删除用户的所有角色关系
   */
  private Mono<Void> deleteUserRoles(Long userId) {
    String sql = "DELETE FROM user_roles WHERE user_id = :userId";
    return databaseClient.sql(sql)
        .bind("userId", userId)
        .fetch()
        .rowsUpdated()
        .then();
  }
}
