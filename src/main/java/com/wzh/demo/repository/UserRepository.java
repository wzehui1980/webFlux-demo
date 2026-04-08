package com.wzh.demo.repository;

import com.wzh.demo.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  // 根据手机号查询用户
  Optional<UserEntity> findByPhone(String phone);

  // 根据用户名查询用户
  Optional<UserEntity> findByName(String name);

  // 分页查询所有用户
  org.springframework.data.domain.Page<UserEntity> findAll(org.springframework.data.domain.Pageable pageable);

  // 自定义查询示例
  @Query("SELECT u FROM UserEntity u WHERE u.name LIKE %:keyword%")
  java.util.List<UserEntity> findByNameContaining(String keyword);
}
