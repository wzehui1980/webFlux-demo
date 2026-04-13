package com.wzh.demo.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类 (R2DBC)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class UserEntity {

	@Id
	private Long id;

	@Column("name")
	private String name;

	@Column("age")
	private Integer age;

	@Column("phone")
	private String phone;

	@Column("email")
	private String email;

	// 用户状态：true-正常，false-屏蔽
	@Column("active")
	private Boolean active = true;

	// 创建时间
	@Column("create_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createTime;

	// 更新时间
	@Column("update_time")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updateTime;

	// 与角色的多对多关系（R2DBC 不支持自动映射，使用 @Transient 标记为非持久化字段）
	@Transient
	private Set<RoleEntity> roles = new HashSet<>();

}
