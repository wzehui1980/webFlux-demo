package com.wzh.demo.controller;

import com.wzh.demo.model.UserEntity;
import com.wzh.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public Flux<UserEntity> findAll() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<UserEntity>> findById(@PathVariable Long id) {
    return userService.findById(id)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<UserEntity> save(@RequestBody UserEntity user) {
    return userService.save(user);
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<UserEntity>> update(@PathVariable Long id, @RequestBody UserEntity user) {
    return userService.update(id, user)
        .map(ResponseEntity::ok)
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteById(@PathVariable Long id) {
    return userService.deleteById(id);
  }

  @GetMapping("/{id}/phone")
  public Mono<String> getPhoneById(@PathVariable Long id) {
    return userService.findById(id)
        .map(UserEntity::getPhone)
        .defaultIfEmpty("未知手机号");
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<UserEntity> streamUsers() {
    return userService.findAll()
        .delayElements(java.time.Duration.ofMillis(500));
  }
}
