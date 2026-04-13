package com.wzh.demo.service;

import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;

  public Flux<RoleEntity> findAllRoles() {
    return roleRepository.findAll();
  }

  public Mono<RoleEntity> findById(Long id) {
    return roleRepository.findById(id);
  }

  public Mono<RoleEntity> findByName(String name) {
    return roleRepository.findByName(name);
  }

  public Mono<RoleEntity> save(RoleEntity role) {
    return roleRepository.findByName(role.getName())
        .flatMap(existingRole -> {
          // 检查是否为更新操作（ID相同）
          if (role.getId() != null && existingRole.getId().equals(role.getId())) {
            return roleRepository.save(role);
          }
          return Mono.error(new RuntimeException("角色名称 '" + role.getName() + "' 已存在"));
        })
        .switchIfEmpty(roleRepository.save(role));
  }

  /**
   * 删除角色
   * 注意：ADMIN 角色不允许删除
   */
  public Mono<Void> deleteById(Long id) {
    return findById(id)
        .flatMap(role -> {
          // 检查是否为 ADMIN 角色
          if ("ADMIN".equalsIgnoreCase(role.getName())) {
            return Mono.error(new RuntimeException("ADMIN 角色不允许删除"));
          }
          // 执行删除
          return roleRepository.deleteById(role.getId());
        });
  }
}
