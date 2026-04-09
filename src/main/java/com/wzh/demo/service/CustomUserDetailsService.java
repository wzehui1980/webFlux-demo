package com.wzh.demo.service;

import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    log.info("=== 尝试登录用户名: {} ===", username);
    return Mono.fromCallable(() -> {
      log.debug("=== 查询数据库中的用户: {} ===", username);
      return userRepository.findByName(username)
          .orElseThrow(() -> {
            log.warn("=== 用户不存在: {} ===", username);
            return new UsernameNotFoundException("用户不存在: " + username);
          });
    })
        .map(userEntity -> {
          log.debug("=== 找到用户: {}, 激活状态: {} ===", userEntity.getName(), userEntity.getActive());
          if (!userEntity.getActive()) {
            log.warn("=== 用户已被屏蔽: {} ===", username);
            throw new DisabledException("用户已被屏蔽: " + username);
          }

          // 获取用户的所有角色
          log.debug("=== 开始加载角色, 角色数量: {} ===", userEntity.getRoles().size());
          List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
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
  }
}
