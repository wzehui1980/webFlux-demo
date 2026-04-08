package com.wzh.demo.service;

import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

  private final UserRepository userRepository;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    System.out.println("=== 尝试登录用户名: " + username + " ===");
    return Mono.fromCallable(() -> {
      System.out.println("=== 查询数据库中的用户: " + username + " ===");
      return userRepository.findByName(username)
          .orElseThrow(() -> {
            System.out.println("=== 用户不存在: " + username + " ===");
            return new RuntimeException("用户不存在: " + username);
          });
    })
        .map(userEntity -> {
          System.out.println("=== 找到用户: " + userEntity.getName() + ", 激活状态: " + userEntity.getActive() + " ===");
          if (!userEntity.getActive()) {
            throw new RuntimeException("用户已被屏蔽: " + username);
          }

          // 获取用户的所有角色
          System.out.println("=== 开始加载角色, 角色数量: " + userEntity.getRoles().size() + " ===");
          List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
              .map(role -> {
                String roleName = "ROLE_" + role.getName();
                System.out.println("=== 添加角色: " + roleName + " ===");
                return new SimpleGrantedAuthority(roleName);
              })
              .collect(Collectors.toList());

          System.out.println("=== 构建 UserDetails, 权限数量: " + authorities.size() + " ===");
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
