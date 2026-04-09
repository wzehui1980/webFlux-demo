package com.wzh.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

	public UserEntity(Object object, String string, int i, String string2, String string3, boolean b, Object object2) {
		// TODO Auto-generated constructor stub
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private Integer age;

	@Column(unique = true, length = 20)
	private String phone;

	@Column(length = 100)
	private String email;

	// 用户状态：true-正常，false-屏蔽
	@Column(nullable = false)
	private Boolean active = true;

	// 创建时间
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createTime;

	// 更新时间
	@UpdateTimestamp
	private LocalDateTime updateTime;

	// 与角色的多对多关系
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<RoleEntity> roles = new HashSet<>();

}
