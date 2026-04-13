package com.wzh.demo.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

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
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createTime;

  // 更新时间
  @Column("update_time")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateTime;

}
