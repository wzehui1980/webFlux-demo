/*
 * @Author: berheley berheley@foxmail.com
 * @Date: 2026-04-07 14:26:11
 * @LastEditors: berheley berheley@foxmail.com
 * @LastEditTime: 2026-04-10 10:56:22
 * @FilePath: \testWebFlux\src\main\java\com\wzh\demo\client\UserClient.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.wzh.demo.client;

import com.wzh.demo.model.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

  private final WebClient userWebClient;

  /**
   * 查询所有用户
   */
  public Flux<UserEntity> findAllUsers() {
    return userWebClient.get()
        .uri("") // 基础路径已包含 /api/users
        .retrieve()
        .bodyToFlux(UserEntity.class);
  }

  /**
   * 根据 ID 查询用户
   */
  public Mono<UserEntity> findUserById(Long id) {
    return userWebClient.get()
        .uri("/{id}", id)
        .retrieve()
        .bodyToMono(UserEntity.class)
        .onErrorResume(e -> {
          log.error("查询用户失败，ID: {}, 错误: {}", id, e.getMessage());
          return Mono.empty();
        });
  }

  /**
   * 保存用户
   */
  public Mono<UserEntity> saveUser(UserEntity user) {
    return userWebClient.post()
        .uri("")
        .bodyValue(user)
        .retrieve()
        .bodyToMono(UserEntity.class);
  }

  /**
   * 更新用户
   */
  public Mono<UserEntity> updateUser(Long id, UserEntity user) {
    Mono<UserEntity> userMono = Mono.just(user);
    return userWebClient.put()
        .uri("/{id}", id)
        .body(userMono, UserEntity.class)
        .retrieve()
        .bodyToMono(UserEntity.class);
  }

  /**
   * 删除用户
   */
  public Mono<Void> deleteUser(Long id) {
    return userWebClient.delete()
        .uri("/{id}", id)
        .retrieve()
        .bodyToMono(Void.class);
  }

  /**
   * 保存用户并获取手机号（flatMap 链式调用示例）
   */
  public Mono<String> saveUserAndGetPhone(UserEntity user) {
    return saveUser(user)
        .flatMap(savedUser -> findUserById(savedUser.getId()))
        .map(UserEntity::getPhone);
  }

  /**
   * 批量查询用户（Flux + flatMap 并发调用）
   */
  public Flux<UserEntity> batchFindUsers(Long... ids) {
    return Flux.fromArray(ids)
        .flatMap(this::findUserById);
  }
}
