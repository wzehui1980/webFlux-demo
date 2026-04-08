package com.wzh.demo.service;

import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  public List<RoleEntity> findAllRoles() {
    return roleRepository.findAll();
  }

  public Mono<RoleEntity> findById(Long id) {
    return Mono.justOrEmpty(roleRepository.findById(id).orElse(null));
  }

  public Mono<RoleEntity> save(RoleEntity role) {
    return Mono.fromCallable(() -> roleRepository.save(role))
        .flatMapMany(Flux::just)
        .next();
  }

  /**
   * 删除角色
   * 注意：ADMIN 角色不允许删除
   * 
   * @param id 角色ID
   * @return Mono<Void>
   */
  public Mono<Void> deleteById(Long id) {
    return findById(id)
        .flatMap(role -> {
          // 检查是否为 ADMIN 角色
          if ("ADMIN".equalsIgnoreCase(role.getName())) {
            return Mono.error(new RuntimeException("ADMIN 角色不允许删除"));
          }
          // 执行删除
          return Mono.fromRunnable(() -> roleRepository.deleteById(id));
        });
  }
}
