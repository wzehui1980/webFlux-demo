# Spring Data JPA 使用指南

## 概述

本项目使用 **Spring Data JPA** 作为数据持久层框架，配合 H2 内存数据库进行演示。JPA 提供了标准的 ORM（对象关系映射）功能，使开发者可以通过操作 Java 对象来完成数据库操作。

## 核心配置

### 1. 依赖配置（pom.xml）

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2. 应用配置（application.yml）

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update  # 自动更新表结构
    show-sql: true      # 显示 SQL 语句
    properties:
      hibernate:
        format_sql: true # 格式化 SQL
```

**ddl-auto 选项说明：**
- `create`: 每次启动都创建新表（数据会丢失）
- `update`: 根据实体类更新表结构（推荐开发环境）
- `validate`: 验证实体类与表结构是否匹配
- `none`: 不做任何操作

## 实体类映射

### UserEntity.java

```java
@Entity
@Table(name = "users")
public class UserEntity {

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
}
```

### 常用注解说明

| 注解 | 说明 | 示例 |
|------|------|------|
| `@Entity` | 标记为 JPA 实体 | - |
| `@Table` | 指定表名 | `@Table(name = "users")` |
| `@Id` | 主键标识 | - |
| `@GeneratedValue` | 主键生成策略 | `IDENTITY`, `AUTO`, `SEQUENCE` |
| `@Column` | 列属性配置 | `nullable`, `unique`, `length` |
| `@Transient` | 不映射到数据库 | - |
| `@Temporal` | 日期类型映射 | `DATE`, `TIME`, `TIMESTAMP` |

## Repository 接口

### UserRepository.java

```java
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // 方法名查询（Spring Data JPA 自动生成实现）
    Optional<UserEntity> findByPhone(String phone);
    
    List<UserEntity> findByNameContaining(String keyword);
    
    List<UserEntity> findByAgeGreaterThan(int age);
    
    // 自定义 JPQL 查询
    @Query("SELECT u FROM UserEntity u WHERE u.name LIKE %:keyword%")
    List<UserEntity> searchUsers(@Param("keyword") String keyword);
    
    // 原生 SQL 查询
    @Query(value = "SELECT * FROM users WHERE age > :age", nativeQuery = true)
    List<UserEntity> findUsersByAge(@Param("age") int age);
}
```

### 方法命名规则

Spring Data JPA 支持通过方法名自动生成查询：

| 关键字 | 示例 | 生成的 SQL |
|--------|------|-----------|
| `findBy` | `findByName(String name)` | `WHERE name = ?` |
| `Containing` | `findByNameContaining(String name)` | `WHERE name LIKE %?%` |
| `GreaterThan` | `findByAgeGreaterThan(int age)` | `WHERE age > ?` |
| `Between` | `findByAgeBetween(int min, int max)` | `WHERE age BETWEEN ? AND ?` |
| `OrderBy` | `findByNameOrderByAgeDesc()` | `ORDER BY age DESC` |
| `And/Or` | `findByNameAndAge(String name, int age)` | `WHERE name = ? AND age = ?` |

## Service 层使用

### UserServiceImpl.java

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // 查询所有用户
    public Flux<UserEntity> findAll() {
        return Flux.fromIterable(userRepository.findAll());
    }

    // 根据 ID 查询
    public Mono<UserEntity> findById(Long id) {
        return Mono.justOrEmpty(userRepository.findById(id).orElse(null))
                .switchIfEmpty(Mono.error(new UserNotFoundException("用户不存在")));
    }

    // 保存用户
    public Mono<UserEntity> save(UserEntity user) {
        return Mono.fromCallable(() -> userRepository.save(user))
                .flatMapMany(Flux::just)
                .next();
    }

    // 更新用户
    public Mono<UserEntity> update(Long id, UserEntity user) {
        return findById(id)
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setAge(user.getAge());
                    return Mono.fromCallable(() -> userRepository.save(existingUser))
                            .flatMapMany(Flux::just)
                            .next();
                });
    }

    // 删除用户
    public Mono<Void> deleteById(Long id) {
        return findById(id)
                .doOnSuccess(user -> userRepository.deleteById(id))
                .then();
    }
}
```

## WebFlux 与 JPA 的整合

### 重要说明

**Spring Data JPA 是阻塞式的**，而 WebFlux 是非阻塞响应式的。在 WebFlux 项目中使用 JPA 时需要注意：

1. **线程调度**: JPA 操作会在独立的线程池中执行，不会阻塞事件循环线程
2. **性能考虑**: 对于高并发场景，建议使用 R2DBC（响应式数据库驱动）
3. **适用场景**: 
   - ✅ 中小规模应用
   - ✅ CPU 密集型操作较少
   - ✅ 已有 JPA 代码迁移
   - ❌ 超高并发场景（建议用 R2DBC）

### 转换方式

```java
// 阻塞式 JPA 调用转换为响应式 Mono/Flux

// 1. 单个对象 → Mono
Mono<UserEntity> mono = Mono.fromCallable(() -> 
    userRepository.findById(id).orElse(null)
);

// 2. 列表 → Flux
Flux<UserEntity> flux = Flux.fromIterable(userRepository.findAll());

// 3. 保存操作 → Mono
Mono<UserEntity> saved = Mono.fromCallable(() -> 
    userRepository.save(user)
);
```

## 数据初始化

### DataInitializer.java

```java
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            userRepository.save(new UserEntity(null, "张三", 20, "13800138000", "zhangsan@example.com"));
            userRepository.save(new UserEntity(null, "李四", 22, "13800138001", "lisi@example.com"));
            userRepository.save(new UserEntity(null, "王五", 25, "13800138002", "wangwu@example.com"));
        }
    }
}
```

## 事务管理

### 声明式事务

```java
@Service
@Transactional  // 类级别事务
public class UserServiceImpl implements UserService {
    
    @Transactional(readOnly = true)  // 只读事务（优化性能）
    public Flux<UserEntity> findAll() {
        return Flux.fromIterable(userRepository.findAll());
    }
    
    @Transactional
    public Mono<UserEntity> save(UserEntity user) {
        // 多个数据库操作会自动在一个事务中
        UserEntity saved = userRepository.save(user);
        // 其他操作...
        return Mono.just(saved);
    }
}
```

### 事务传播行为

- `REQUIRED`（默认）: 如果存在事务则加入，否则创建新事务
- `REQUIRES_NEW`: 总是创建新事务
- `SUPPORTS`: 如果存在事务则加入，否则非事务执行
- `NOT_SUPPORTED`: 非事务执行
- `MANDATORY`: 必须在事务中执行
- `NEVER`: 不能在事务中执行

## 高级查询示例

### 1. 分页查询

```java
// Repository
Page<UserEntity> findByAgeGreaterThan(int age, Pageable pageable);

// Service
public Mono<Page<UserEntity>> findUsersByAge(int age, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("age").descending());
    return Mono.fromCallable(() -> userRepository.findByAgeGreaterThan(age, pageable));
}
```

### 2. 多表关联查询

```java
@Entity
public class OrderEntity {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}

// Repository
@Query("SELECT o FROM OrderEntity o JOIN FETCH o.user WHERE o.user.id = :userId")
List<OrderEntity> findByUserIdWithUser(@Param("userId") Long userId);
```

### 3. 动态查询（Criteria API）

```java
public List<UserEntity> findUsers(String name, Integer minAge, Integer maxAge) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
    Root<UserEntity> root = query.from(UserEntity.class);
    
    List<Predicate> predicates = new ArrayList<>();
    
    if (name != null) {
        predicates.add(cb.like(root.get("name"), "%" + name + "%"));
    }
    if (minAge != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("age"), minAge));
    }
    if (maxAge != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("age"), maxAge));
    }
    
    query.where(predicates.toArray(new Predicate[0]));
    return entityManager.createQuery(query).getResultList();
}
```

## 性能优化建议

1. **使用懒加载**: `@ManyToOne(fetch = FetchType.LAZY)`
2. **避免 N+1 查询**: 使用 `JOIN FETCH` 或 `@EntityGraph`
3. **批量操作**: 使用 `saveAll()` 代替多次 `save()`
4. **合理索引**: 为常用查询字段添加索引
5. **缓存**: 启用二级缓存（Hibernate Cache）
6. **连接池**: 配置合适的数据库连接池大小

## 常见问题

### Q1: JPA 在 WebFlux 中会阻塞吗？

A: JPA 本身是阻塞的，但 Spring 会将其调度到专门的线程池（boundedElastic）执行，不会阻塞 WebFlux 的事件循环线程。

### Q2: 什么时候使用 R2DBC？

A: 当你的应用需要处理超高并发（如每秒数千请求），且数据库操作频繁时，建议使用 R2DBC。

### Q3: 如何处理事务？

A: 使用 `@Transactional` 注解，Spring 会自动管理事务的开启、提交和回滚。

### Q4: 如何查看执行的 SQL？

A: 在 application.yml 中配置 `spring.jpa.show-sql: true` 和 `format_sql: true`。

## 参考资源

- [Spring Data JPA 官方文档](https://spring.io/projects/spring-data-jpa)
- [Hibernate ORM 文档](https://hibernate.org/orm/documentation/)
- [JPA 规范](https://jakarta.ee/specifications/persistence/)
