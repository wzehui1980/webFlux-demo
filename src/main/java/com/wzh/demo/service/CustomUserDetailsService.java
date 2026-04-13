package com.wzh.demo.service;

import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.RoleRepository;
import com.wzh.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final DatabaseClient databaseClient;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    log.info("=== 尝试登录用户名: {} ===", username);

    return userRepository.findByName(username)
        .switchIfEmpty(Mono.error(new UsernameNotFoundException("用户不存在: " + username)))
        .flatMap(userEntity -> {
          log.debug("=== 找到用户: {}, 激活状态: {} ===", userEntity.getName(), userEntity.getActive());

          if (!userEntity.getActive()) {
            log.warn("=== 用户已被屏蔽: {} ===", username);
            return Mono.error(new DisabledException("用户已被屏蔽: " + username));
          }

          // 手动查询用户的角色
          return loadUserRoles(userEntity.getId())
              .collectList()
              .doOnNext(roles -> {
                userEntity.setRoles(new HashSet<>(roles));
                log.debug("=== 加载角色数量: {} ===", roles.size());
              })
              .map(roles -> {
                // 获取用户的所有角色并转换为权限
                var authorities = roles.stream()
                    .map(role -> {
                      String roleName = "ROLE_" + role.getName();
                      log.debug("=== 添加角色: {} ===", roleName);
                      return new SimpleGrantedAuthority(roleName);
                    })
                    .collect(Collectors.toList());

                log.debug("=== 构建 UserDetails, 权限数量: {} ===", authorities.size());

                return (UserDetails) User.builder()
                    .username(userEntity.getName())
                    .password("") // 密码为空，演示环境不验证密码
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
              });
        });
  }

  /**
   * 手动加载用户的角色列表
   */
  private Flux<RoleEntity> loadUserRoles(Long userId) {
    String sql = "SELECT r.* FROM roles r INNER JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = :userId";

    return databaseClient.sql(sql)
        .bind("userId", userId)
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
        .all();
  }
}
