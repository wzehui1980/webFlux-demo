package com.wzh.demo.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色实体类 (R2DBC)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("roles")
public class RoleEntity {

  @Id
  private Long id;

  @Column("name")
  private String name;

  @Column("description")
  private String description;

  // 创建时间
  @Column("create_time")
  private LocalDateTime createTime;

  // 更新时间
  @Column("update_time")
  private LocalDateTime updateTime;

  // 与用户的多对多关系（R2DBC 不支持反向映射，这里仅用于展示）
  @MappedCollection(idColumn = "role_id", keyColumn = "user_id")
  private Set<UserEntity> users = new HashSet<>();

}
