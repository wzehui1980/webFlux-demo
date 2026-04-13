package com.wzh.demo.service;

import com.wzh.demo.model.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

  Flux<UserEntity> findAll();

  Mono<UserEntity> findById(Long id);

  Mono<UserEntity> save(UserEntity user);

  Mono<UserEntity> update(Long id, UserEntity user);

  Mono<Void> deleteById(Long id);

  Mono<UserEntity> findByPhone(String phone);

  // 屏蔽/激活用户
  Mono<UserEntity> toggleUserStatus(Long id);
}
