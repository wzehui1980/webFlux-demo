package com.wzh.demo.repository;

import com.wzh.demo.model.RoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<RoleEntity, Long> {

  Mono<RoleEntity> findByName(String name);

  @Override
  Flux<RoleEntity> findAll();
}
