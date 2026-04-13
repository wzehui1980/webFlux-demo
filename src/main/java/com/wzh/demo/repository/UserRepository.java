package com.wzh.demo.repository;

import com.wzh.demo.model.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {

  // 根据手机号查询用户
  Mono<UserEntity> findByPhone(String phone);

  // 根据用户名查询用户
  Mono<UserEntity> findByName(String name);

  // 查询所有用户（返回 Flux）
  @Override
  Flux<UserEntity> findAll();

  // 自定义查询示例
  @Query("SELECT * FROM users WHERE name LIKE :keyword")
  Flux<UserEntity> findByNameContaining(String keyword);
}
