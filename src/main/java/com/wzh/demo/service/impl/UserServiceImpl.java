package com.wzh.demo.service.impl;

import com.wzh.demo.exception.UserNotFoundException;
import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.UserRepository;
import com.wzh.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public Flux<UserEntity> findAll() {
    return Flux.fromIterable(userRepository.findAll());
  }

  @Override
  public Mono<UserEntity> findById(Long id) {
    return Mono.justOrEmpty(userRepository.findById(id).orElse(null))
        .switchIfEmpty(Mono.error(new UserNotFoundException("用户ID=" + id + "不存在")));
  }

  @Override
  public Mono<UserEntity> save(UserEntity user) {
    return Mono.justOrEmpty(userRepository.findByPhone(user.getPhone()).orElse(null))
        .flatMap(existingUser -> Mono.<UserEntity>error(new RuntimeException("手机号" + user.getPhone() + "已被注册")))
        .switchIfEmpty(Mono.fromCallable(() -> userRepository.save(user)).flatMapMany(Flux::just).next());
  }

  @Override
  public Mono<UserEntity> update(Long id, UserEntity user) {
    return findById(id)
        .flatMap(existingUser -> {
          if (!existingUser.getPhone().equals(user.getPhone())) {
            return Mono.justOrEmpty(userRepository.findByPhone(user.getPhone()).orElse(null))
                .flatMap(phoneUser -> Mono.<UserEntity>error(new RuntimeException("手机号" + user.getPhone() + "已被注册")))
                .switchIfEmpty(Mono.just(existingUser));
          }
          return Mono.just(existingUser);
        })
        .flatMap(existingUser -> {
          System.out.println("=== 开始更新用户: " + existingUser.getName() + " ===");
          System.out.println("=== 新角色数量: " + (user.getRoles() != null ? user.getRoles().size() : 0) + " ===");

          existingUser.setName(user.getName());
          existingUser.setAge(user.getAge());
          existingUser.setPhone(user.getPhone());
          existingUser.setEmail(user.getEmail());
          existingUser.setActive(user.getActive());

          // 清空旧的角色关系
          existingUser.getRoles().clear();
          System.out.println("=== 已清空旧角色 ===");

          // 添加新的角色
          if (user.getRoles() != null) {
            existingUser.getRoles().addAll(user.getRoles());
            System.out.println("=== 添加了 " + user.getRoles().size() + " 个新角色 ===");
          }

          System.out.println("=== 保存前的角色数量: " + existingUser.getRoles().size() + " ===");

          // 手动设置更新时间，确保 @UpdateTimestamp 生效
          existingUser.setUpdateTime(java.time.LocalDateTime.now());

          return Mono.fromCallable(() -> userRepository.save(existingUser)).flatMapMany(Flux::just).next();
        });
  }

  @Override
  public Mono<Void> deleteById(Long id) {
    return findById(id)
        .flatMap(user -> Mono.fromRunnable(() -> userRepository.deleteById(id)));
  }

  @Override
  public Mono<UserEntity> findByPhone(String phone) {
    return Mono.justOrEmpty(userRepository.findByPhone(phone).orElse(null));
  }

  @Override
  public Page<UserEntity> findUsersWithPage(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public Mono<UserEntity> toggleUserStatus(Long id) {
    return findById(id)
        .flatMap(user -> {
          user.setActive(!user.getActive());
          // 手动设置更新时间
          user.setUpdateTime(java.time.LocalDateTime.now());
          return Mono.fromCallable(() -> userRepository.save(user)).flatMapMany(Flux::just).next();
        });
  }
}
