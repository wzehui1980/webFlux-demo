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
	private LocalDateTime createTime;

	// 更新时间
	@Column("update_time")
	private LocalDateTime updateTime;

	// 与角色的多对多关系（R2DBC 不支持 @ManyToMany，需要手动管理）
	@MappedCollection(idColumn = "user_id", keyColumn = "role_id")
	private Set<RoleEntity> roles = new HashSet<>();

}
