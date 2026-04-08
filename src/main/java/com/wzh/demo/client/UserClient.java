package com.wzh.demo.client;

import com.wzh.demo.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class UserClient {

  private final WebClient webClient;

  public Flux<UserEntity> findAllUsers() {
    return webClient.get()
        .uri("/users")
        .retrieve()
        .bodyToFlux(UserEntity.class);
  }

  public Mono<UserEntity> findUserById(Long id) {
    return webClient.get()
        .uri("/users/{id}", id)
        .retrieve()
        .bodyToMono(UserEntity.class)
        .onErrorResume(e -> {
          System.out.println("查询用户失败：" + e.getMessage());
          return Mono.empty();
        });
  }

  public Mono<UserEntity> saveUser(UserEntity user) {
    return webClient.post()
        .uri("/users")
        .bodyValue(user)
        .retrieve()
        .bodyToMono(UserEntity.class);
  }

  public Mono<UserEntity> updateUser(Long id, UserEntity user) {
    return webClient.put()
        .uri("/users/{id}", id)
        .bodyValue(user)
        .retrieve()
        .bodyToMono(UserEntity.class);
  }

  public Mono<Void> deleteUser(Long id) {
    return webClient.delete()
        .uri("/users/{id}", id)
        .retrieve()
        .bodyToMono(Void.class);
  }

  public Mono<String> saveUserAndGetPhone(UserEntity user) {
    return saveUser(user)
        .flatMap(savedUser -> findUserById(savedUser.getId()))
        .map(UserEntity::getPhone);
  }

  public Flux<UserEntity> batchFindUsers(Long... ids) {
    return Flux.fromArray(ids)
        .flatMap(this::findUserById);
  }
}
