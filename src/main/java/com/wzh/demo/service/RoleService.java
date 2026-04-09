package com.wzh.demo.service;

import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  /**
   * 分页查询角色
   * 
   * @param pageable 分页参数
   * @return 分页结果
   */
  public Page<RoleEntity> findRolesWithPage(Pageable pageable) {
    return roleRepository.findAll(pageable);
  }

  public Mono<RoleEntity> findById(Long id) {
    return Mono.justOrEmpty(roleRepository.findById(id).orElse(null));
  }

  /**
   * 根据名称查询角色
   * 
   * @param name 角色名称
   * @return Mono<RoleEntity>
   */
  public Mono<RoleEntity> findByName(String name) {
    return Mono.justOrEmpty(roleRepository.findByName(name).orElse(null));
  }

  public Mono<RoleEntity> save(RoleEntity role) {
    return Mono.fromCallable(() -> {
      // 检查角色名称是否已存在（新增或修改时）
      if (role.getId() == null) {
        // 新增角色：检查名称是否重复
        if (roleRepository.findByName(role.getName()).isPresent()) {
          throw new RuntimeException("角色名称 '" + role.getName() + "' 已存在");
        }
      } else {
        // 更新角色：检查新名称是否与其他角色重复
        roleRepository.findByName(role.getName())
            .ifPresent(existingRole -> {
              if (!existingRole.getId().equals(role.getId())) {
                throw new RuntimeException("角色名称 '" + role.getName() + "' 已被其他角色使用");
              }
            });
      }
      return roleRepository.save(role);
    })
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
