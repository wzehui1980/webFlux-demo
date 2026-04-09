# Spring WebFlux 实战学习项目

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目简介

本项目是一个完整的 **Spring WebFlux** 响应式编程学习示例，旨在帮助开发者快速掌握 Spring 响应式 Web 开发的核心概念和最佳实践。项目采用分层架构设计，涵盖了从基础概念到实际应用的完整技术栈。

### 📚 配套教程

本项目基于 [Spring WebFlux 完整实操教程](Spring%20WebFlux%20完整实操教程（Web业务开发+REST接口服务及调用）.md) 开发，该教程详细讲解了：

- ✅ WebFlux 环境搭建与配置
- ✅ REST 接口开发（服务端）
- ✅ WebClient 客户端调用
- ✅ 用户登录认证（Spring Security）
- ✅ 异常处理与全局错误管理
- ✅ map vs flatMap 深度解析
- ✅ 单元测试与集成测试
- ✅ 进阶优化与最佳实践

**建议先阅读教程，再结合本 DEMO 项目进行实践学习。**

### ✨ 核心特性

- 🚀 **响应式编程**：基于 Project Reactor 的 Mono/Flux 异步流处理
- 🏗️ **分层架构**：Controller → Service → Repository 清晰的分层设计
- 🔧 **完整 CRUD**：用户管理的增删改查全流程实现
- ⚡ **非阻塞 I/O**：高性能的异步非阻塞请求处理
- 🌊 **SSE 支持**：Server-Sent Events 实时数据推送
- 🧪 **单元测试**：使用 StepVerifier 进行响应式流测试
- 🔌 **WebClient**：响应式 HTTP 客户端调用示例
- 🛡️ **全局异常处理**：统一的错误响应机制
- 🔐 **登录认证**：Spring Security 实现用户登录和权限控制
- 👥 **角色管理**：支持多角色分配和基于角色的访问控制（RBAC）
- 📄 **分页功能**：用户列表支持分页显示
- 🎨 **美观界面**：Thymeleaf 模板引擎打造的现代化后台管理界面

## 🛠️ 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 开发语言 |
| Spring Boot | 3.2.5 | 应用框架 |
| Spring WebFlux | - | 响应式 Web 框架（Netty 容器） |
| Spring Data JPA | - | ORM 数据访问层 |
| Spring Security | - | 安全认证和授权 |
| Thymeleaf | - | 模板引擎 |
| Project Reactor | - | 响应式流库 |
| Lombok | - | 代码简化工具 |
| H2 Database | - | 内存数据库（演示用） |
| Maven | 3.6+ | 项目构建工具 |

## 📁 项目结构

```
testWebFlux/
├── src/
│   ├── main/
│   │   ├── java/com/wzh/demo/
│   │   │   ├── client/              # WebClient 客户端
│   │   │   │   └── UserClient.java
│   │   │   ├── config/              # 配置类
│   │   │   │   ├── WebClientConfig.java
│   │   │   │   ├── SecurityConfig.java      # 安全配置
│   │   │   │   └── DataInitializer.java     # 数据初始化
│   │   │   ├── controller/          # REST 控制器
│   │   │   │   ├── HelloController.java     # 基础示例
│   │   │   │   ├── UserController.java      # 用户 API
│   │   │   │   ├── AdminController.java     # 后台管理
│   │   │   │   └── LoginController.java     # 登录页面
│   │   │   ├── exception/           # 异常处理
│   │   │   │   ├── UserNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── model/               # 实体类
│   │   │   │   ├── UserEntity.java
│   │   │   │   └── RoleEntity.java          # 角色实体
│   │   │   ├── repository/          # 数据访问层
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── RoleRepository.java      # 角色仓库
│   │   │   ├── service/             # 业务逻辑层
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RoleService.java         # 角色服务
│   │   │   │   ├── CustomUserDetailsService.java  # 用户详情服务
│   │   │   │   └── impl/
│   │   │   │       └── UserServiceImpl.java
│   │   │   └── TestWebFluxApplication.java  # 启动类
│   │   └── resources/
│   │       ├── application.yml      # 配置文件
│   │       └── templates/           # Thymeleaf 模板
│   │           ├── login.html       # 登录页面
│   │           └── admin/           # 后台管理页面
│   │               ├── user-list.html   # 用户列表
│   │               ├── user-form.html   # 用户表单
│   │               ├── role-list.html   # 角色列表
│   │               └── role-form.html   # 角色表单
│   └── test/
│       └── java/com/wzh/demo/
│           └── TestWebFluxApplicationTests.java  # 单元测试
├── pom.xml                          # Maven 配置
├── README.md                        # 项目说明
└── Spring WebFlux 完整实操教程.md   # 完整教程
```

## 🚀 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.6+
- IDE（推荐 IntelliJ IDEA 或 VS Code）

### 安装与运行

1. **克隆项目**
```bash
git clone https://github.com/your-username/testWebFlux.git
cd testWebFlux
```

2. **编译项目**
```bash
mvn clean install
```

3. **启动应用**
```bash
mvn spring-boot:run
```

4. **验证启动**

访问 http://localhost:8080/api/hello，如果返回 `Hello 哈哈`，说明启动成功。

## 📚 API 文档

### 基础示例接口

| 方法 | 路径 | 描述 | 示例 |
|------|------|------|------|
| GET | `/api/hello` | 基础字符串响应 | `/api/hello?key=World` |
| GET | `/api/helloMono/{name}` | Mono 响应示例 | `/api/helloMono/Alice` |
| GET | `/api/helloMap` | Map 数据响应 | `/api/helloMap` |
| GET | `/api/helloFlux` | Flux 多元素响应 | `/api/helloFlux` |
| GET | `/api/helloFlux2` | Flux 对象流响应 | `/api/helloFlux2` |
| GET | `/api/see` | SSE 流式推送 | `/api/see` |

### 用户管理接口

#### 查询所有用户
```http
GET /api/users
```
**响应示例：**
```json
[
  {
    "id": 1,
    "name": "张三",
    "age": 20,
    "phone": "13800138000",
    "email": "zhangsan@example.com"
  },
  {
    "id": 2,
    "name": "李四",
    "age": 22,
    "phone": "13800138001",
    "email": "lisi@example.com"
  }
]
```

#### 根据 ID 查询用户
```http
GET /api/users/{id}
```
**响应示例：**
```json
{
  "id": 1,
  "name": "张三",
  "age": 20,
  "phone": "13800138000",
  "email": "zhangsan@example.com"
}
```

#### 新增用户
```http
POST /api/users
Content-Type: application/json

{
  "name": "赵六",
  "age": 28,
  "phone": "13800138003",
  "email": "zhaoliu@example.com"
}
```
**响应状态码：** `201 Created`

#### 修改用户
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "name": "赵六666",
  "age": 29,
  "phone": "13800138004",
  "email": "zhaoliu666@example.com"
}
```

#### 删除用户
```http
DELETE /api/users/{id}
```
**响应状态码：** `204 No Content`

#### 查询用户手机号
```http
GET /api/users/{id}/phone
```
**响应示例：**
```
13800138000
```

#### SSE 流式推送
```http
GET /api/users/stream
Accept: text/event-stream
```

### 后台管理系统

启动项目后访问 http://localhost:8080/login 进入登录页面。

**测试账号：**
- 用户名：`admin`（管理员，进入后台管理）
- 用户名：`张三`、`李四`、`王五`（普通用户，进入欢迎页面）
- 密码：任意（演示环境）

**登录后跳转规则：**
- ✅ ADMIN 角色 → 后台管理页面（/admin/users）
- ✅ 其他角色 → 欢迎页面（/welcome），显示教程内容

#### 用户管理功能

1. **用户列表**：分页显示所有用户，支持查看用户角色和状态
2. **新增用户**：填写用户信息，可分配多个角色
3. **编辑用户**：修改用户信息和角色
4. **删除用户**：确认后删除用户
5. **屏蔽/激活**：一键切换用户状态

#### 角色管理功能

1. **角色列表**：查看所有角色及其描述
2. **新增角色**：创建自定义角色（如 MANAGER、EDITOR 等）
3. **编辑角色**：修改角色名称和描述
4. **删除角色**：删除不需要的角色

**默认角色：**
- `ADMIN`：系统管理员，拥有所有权限
- `USER`：普通用户，拥有基本权限
- `MANAGER`：经理，拥有管理权限

### WebFlux Security 配置

本项目使用 **Spring WebFlux Security**（而非 Servlet Security），关键配置如下：

#### 1. 响应式安全配置
```java
@Configuration
@EnableWebFluxSecurity  // 注意：不是 @EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
        return (webFilterExchange, authentication) -> {
            // 根据角色决定跳转页面
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            String redirectUrl = isAdmin ? "/admin/users" : "/welcome";
            
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create(redirectUrl));
            return webFilterExchange.getExchange().getResponse().setComplete();
        };
    }
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(auth -> auth
                .pathMatchers("/login", "/welcome").permitAll()
                .pathMatchers("/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .authenticationSuccessHandler(authenticationSuccessHandler()));
        return http.build();
    }
}
```

#### 2. 响应式用户详情服务
```java
@Service
public class CustomUserDetailsService implements ReactiveUserDetailsService {
    
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return Mono.fromCallable(() -> userRepository.findByName(username))
            .map(userEntity -> {
                // 构建 UserDetails
                return User.builder()
                    .username(userEntity.getName())
                    .password("")
                    .authorities(authorities)
                    .build();
            });
    }
}
```

**关键点：**
- ✅ 使用 `@EnableWebFluxSecurity` 而非 `@EnableWebSecurity`
- ✅ 使用 `ServerHttpSecurity` 而非 `HttpSecurity`
- ✅ 实现 `ReactiveUserDetailsService` 而非 `UserDetailsService`
- ✅ 返回 `Mono<UserDetails>` 而非 `UserDetails`
- ✅ 使用 Netty 容器（无需 Tomcat）

## 🧪 测试

### 运行单元测试
```bash
mvn test
```

### 使用 Postman 测试

1. 导入 Postman 集合（可选）
2. 按照上述 API 文档发送请求
3. 观察响应结果

### 访问 H2 控制台

由于 WebFlux 项目使用 Netty 容器，H2 Console 需要在独立端口启动。

**访问地址：** http://localhost:8082

- **JDBC URL:** `jdbc:h2:mem:testdb`
- **用户名:** `sa`
- **密码:** (留空)

**注意：** H2 Console 在应用启动时自动启动，无需额外配置。

## 💡 核心概念

### JPA 实体映射

本项目使用 **Spring Data JPA** 进行数据持久化，通过注解方式将 Java 对象映射到数据库表：

#### 用户实体（UserEntity）

```java
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Column(unique = true, length = 20)
    private String phone;
    
    // 与角色的多对多关系
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();
}
```

#### 角色实体（RoleEntity）

```java
@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String name;
    
    @Column(length = 200)
    private String description;
    
    // 与用户的多对多关系
    @ManyToMany(mappedBy = "roles")
    private Set<UserEntity> users = new HashSet<>();
    
    // 重写equals和hashCode，排除users字段避免懒加载问题
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleEntity that = (RoleEntity) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(name, that.name) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }
}
```

**⚠️ 重要提示：避免懒加载异常**

在多对多关系中，`RoleEntity.users` 集合默认是懒加载的。当尝试计算 `HashSet` 的 `hashCode` 时，如果 Session 已关闭会抛出 `LazyInitializationException`。

**解决方案：**
1. 手动实现 `equals()` 和 `hashCode()`，排除 `users` 字段
2. 不使用 Lombok 的 `@Data` 注解（它会自动生成包含所有字段的 hashCode）
3. 或者在查询时使用 `@EntityGraph` 急加载

**常用注解：**
- `@Entity`: 标记为 JPA 实体
- `@Table`: 指定数据库表名
- `@Id`: 主键标识
- `@GeneratedValue`: 主键生成策略
- `@Column`: 列属性配置（nullable、unique、length 等）
- `@ManyToMany`: 多对多关系映射
- `@JoinTable`: 指定中间表

### Mono vs Flux

- **Mono<T>**: 表示 0 或 1 个元素的异步序列
  ```java
  Mono<String> mono = Mono.just("Hello");
  Mono<User> user = userRepository.findById(1L);
  ```

- **Flux<T>**: 表示 0 到 N 个元素的异步序列
  ```java
  Flux<String> flux = Flux.just("A", "B", "C");
  Flux<User> users = userRepository.findAll();
  ```

### map vs flatMap

- **map**: 同步转换，用于普通对象转换
  ```java
  // 从 User 中提取 name
  monoUser.map(User::getName)
  ```

- **flatMap**: 异步转换，用于处理返回 Mono/Flux 的操作
  ```java
  // 异步查询后继续异步操作
  monoUser.flatMap(user -> userService.findByPhone(user.getPhone()))
  ```

### 常用操作符

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `map` | 同步转换 | `.map(User::getName)` |
| `flatMap` | 异步转换 | `.flatMap(user -> findById(user.getId()))` |
| `switchIfEmpty` | 空值默认处理 | `.switchIfEmpty(Mono.error(...))` |
| `filter` | 过滤元素 | `.filter(user -> user.getAge() > 18)` |
| `delayElements` | 延迟发送 | `.delayElements(Duration.ofMillis(500))` |
| `onErrorResume` | 错误恢复 | `.onErrorResume(e -> Mono.empty())` |

## 🎯 学习要点

### 响应式编程思维

✅ **异步非阻塞**：所有操作都是异步的，不会阻塞线程  
✅ **流式处理**：数据以流的形式传递和处理  
✅ **背压支持**：自动处理生产者和消费者的速度差异  
✅ **函数式风格**：链式调用，代码简洁优雅

### 常见陷阱

❌ **不要在响应式链中使用阻塞操作**
```java
// 错误示例
.flatMap(user -> {
    Thread.sleep(1000); // ❌ 阻塞操作
    return Mono.just(user);
})

// 正确示例
.flatMap(user -> Mono.delay(Duration.ofSeconds(1)).thenReturn(user))
```

❌ **不要混合使用同步和异步代码**
```java
// 错误示例
User user = userRepository.findById(1L).block(); // ❌ 阻塞等待

// 正确示例
userRepository.findById(1L).flatMap(user -> ...) // ✅ 链式调用
```

✅ **使用 flatMap 处理异步嵌套**
```java
// 先查询用户，再根据用户信息查询详情
findById(id)
    .flatMap(user -> findDetailByUserId(user.getId()))
```

✅ **使用 switchIfEmpty 处理空值**
```java
findById(id)
    .switchIfEmpty(Mono.error(new UserNotFoundException("用户不存在")))
```

## 🔍 性能优势

与传统 Spring MVC 相比，WebFlux 的优势：

| 特性 | Spring MVC | Spring WebFlux |
|------|-----------|----------------|
| I/O 模型 | 阻塞式 | 非阻塞式 |
| 线程模型 | 每个请求一个线程 | 少量事件循环线程 |
| 并发能力 | 受线程池限制 | 更高并发 |
| 资源消耗 | 较高 | 较低 |
| 适用场景 | CPU 密集型 | I/O 密集型、高并发 |

## 📖 扩展学习

### 进阶主题

1. **集成响应式数据库**
   - Spring Data R2DBC（真正的响应式关系型数据库）
   - Spring Data MongoDB Reactive
   - 注意：JPA 是阻塞的，在 WebFlux 中需要通过调度器适配

2. **JPA 高级特性**
   - JPQL 查询语言
   - Criteria API 动态查询
   - 实体关系映射（@OneToMany、@ManyToOne、@ManyToMany 等）
   - 事务管理（@Transactional）
   - 懒加载与急加载
   - 多对多关系实践（用户-角色）

3. **安全认证**
   - Spring Security 集成
   - 基于角色的访问控制（RBAC）
   - 自定义 UserDetailsService
   - 登录页面定制
   - JWT 无状态认证（进阶）

4. **高级特性**
   - 分页查询（Pageable）
   - 缓存集成（Redis Reactive）
   - WebSocket 支持

5. **微服务架构**
   - Spring Cloud Gateway
   - 服务间调用（WebClient）
   - 熔断降级（Resilience4j）

### 推荐资源

- 📘 [Spring WebFlux 官方文档](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- 📘 [Project Reactor 参考指南](https://projectreactor.io/docs/core/release/reference/)
- 📘 **[本项目配套教程](Spring%20WebFlux%20完整实操教程（Web业务开发+REST接口服务及调用）.md)** - 完整的 WebFlux 实操指南
- 🎥 [响应式编程入门教程](https://www.baeldung.com/spring-webflux)
- 🎥 [Reactor Core 官方示例](https://github.com/reactor/lite-rx-api-hands-on)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 👤 作者

**泽汇**

- GitHub: [@wzehui1980](https://github.com/wzehui1980)

## 🙏 致谢

感谢以下开源项目：

- [Spring Framework](https://spring.io/)
- [Project Reactor](https://projectreactor.io/)
- [Lombok](https://projectlombok.org/)

---

⭐ 如果这个项目对你有帮助，请给个 Star！
