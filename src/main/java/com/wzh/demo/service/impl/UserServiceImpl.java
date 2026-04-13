package com.wzh.demo.service.impl;

import com.wzh.demo.exception.UserNotFoundException;
import com.wzh.demo.model.UserEntity;
import com.wzh.demo.repository.UserRepository;
import com.wzh.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public Flux<UserEntity> findAll() {
    return userRepository.findAll();
  }

  @Override
  public Mono<UserEntity> findById(Long id) {
    return userRepository.findById(id)
        .switchIfEmpty(Mono.error(new UserNotFoundException("用户ID=" + id + "不存在")));
  }

  @Override
  public Mono<UserEntity> save(UserEntity user) {
    return userRepository.findByPhone(user.getPhone())
        .flatMap(existingUser -> Mono.<UserEntity>error(new RuntimeException("手机号" + user.getPhone() + "已被注册")))
        .switchIfEmpty(
            userRepository.save(user));
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

          return userRepository.save(existingUser);
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
        .switchIfEmpty(Mono.error(new UserNotFoundException("手机号=" + phone + "的用户不存在")));
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
}
