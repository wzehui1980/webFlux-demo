# Spring WebFlux 完整实操教程（Web业务开发+REST接口服务及调用）

> **版本更新说明**：本教程已更新至最新版本，新增了登录认证、角色管理、后台管理系统等完整功能模块。

# 一、教程前言

本教程面向有 Spring Boot 基础、想入门 Spring WebFlux 的开发者，全程实操落地，涵盖「环境搭建→REST接口开发→接口调用（客户端）→异常处理→登录认证→角色管理→后台系统→进阶优化」，所有代码可直接复制运行，重点解决 WebFlux 响应式开发的核心痛点（异步非阻塞、流处理），同时对比传统 Spring MVC，帮助快速理解差异。

核心目标：掌握 WebFlux 开发 REST 接口的完整流程，能独立完成服务端开发和客户端调用，理解响应式编程的实际应用场景（如高并发IO、远程调用），并能够构建完整的后台管理系统。

技术栈说明：Spring Boot 3.2.x（WebFlux 依赖）、Java 17+、Lombok（简化代码）、Postman（接口测试）、WebClient（客户端调用）、H2 内存数据库、Spring Security（安全认证）、Thymeleaf（模板引擎）、Spring Data JPA（数据持久化）、Netty（响应式容器）。

# 二、环境搭建（第一步：基础工程创建）

## 2.1 创建 Spring Boot 工程

方式1：通过 Spring Initializr（https://start.spring.io/）创建，方式2：IDEA 直接新建 Spring Boot 项目，核心依赖如下（适配 Spring Boot 3.2.x 版本，Java 需至少 17）：

### 2.1.1 核心依赖（pom.xml）

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
    <relativePath/>
</parent>

<dependencies>
    <!-- WebFlux 核心依赖（替代 Spring MVC） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Thymeleaf 模板引擎 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>

    <!-- Lombok 简化代码 -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- H2 内存数据库 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- 测试依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.1.2 配置文件（application.yml）

```yaml
server:
  port: 8080

spring:
  application:
    name: webflux-demo
  h2:
    console:
      enabled: true
      path: /h2-console
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password:
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### 2.1.3 启动类

```java
package com.example.webfluxdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebfluxDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebfluxDemoApplication.class, args);
    }
}
```

### 2.1.4 环境测试

启动项目，访问 http://localhost:8080/h2-console，输入配置的数据库地址和账号密码，可正常进入控制台说明环境搭建成功。

# 三、实体类设计（支持多对多关系）

## 3.1 用户实体（UserEntity）

```java
package com.example.webfluxdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    
    // 用户状态：true-正常，false-屏蔽
    @Column(nullable = false)
    private Boolean active = true;
    
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

## 3.2 角色实体（RoleEntity）

**⚠️ 重要：避免懒加载异常！**

```java
package com.example.webfluxdemo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
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
    
    // 不包含users的构造函数（避免循环引用）
    public RoleEntity(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<UserEntity> getUsers() { return users; }
    public void setUsers(Set<UserEntity> users) { this.users = users; }

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

**为什么需要手动实现 equals/hashCode？**

在多对多关系中，`RoleEntity.users` 集合默认是懒加载的。当尝试将 RoleEntity 放入 HashSet 或 HashMap 时，会调用 `hashCode()` 方法。如果 Lombok 的 `@Data` 注解生成的 `hashCode()` 包含 `users` 字段，而 Session 已关闭，就会抛出 `LazyInitializationException`。

**解决方案：**
1. ✅ 手动实现 `equals()` 和 `hashCode()`，排除 `users` 字段
2. ✅ 不使用 Lombok 的 `@Data` 注解
3. ✅ 或者在查询时使用 `@EntityGraph` 急加载

# 四、数据访问层（Repository）

## 4.1 用户仓库（UserRepository）

```java
package com.example.webfluxdemo.repository;

import com.example.webfluxdemo.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByPhone(String phone);
    Optional<UserEntity> findByName(String name);
    Page<UserEntity> findAll(Pageable pageable);
}
```

## 4.2 角色仓库（RoleRepository）

```java
package com.example.webfluxdemo.repository;

import com.example.webfluxdemo.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
```

# 五、REST 接口开发（服务端）

## 5.1 HelloController - Mono/Flux 基础示例

```java
package com.wzh.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class HelloController {

    // 1. 简单字符串返回
    @GetMapping("/hello")
    public String hello(@RequestParam(name = "key", required = false, defaultValue = "哈哈") String key) {
        return "Hello " + key;
    }

    // 2. Mono<String> 返回单个对象
    @GetMapping("/helloMono/{name}")
    public Mono<String> helloMono(@PathVariable String name) {
        return Mono.just("Hello Mono " + name);
    }

    // 3. Mono<Map> 返回复杂对象
    @GetMapping("/helloMap")
    public Mono<Map> helloMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("name", "WebFlux");
        map.put("version", "3.2.5");
        return Mono.just(map);
    }

    // 4. Flux<String> 返回多个元素
    @GetMapping("/helloFlux")
    public Flux<String> helloFlux() {
        return Flux.just("Hello", "Flux", "Stream");
    }

    // 5. Flux<UserEntity> 返回对象流
    @GetMapping("/helloFlux2")
    public Flux<UserEntity> helloFlux2() {
        UserEntity user1 = new UserEntity();
        user1.setName("Alice");
        user1.setAge(30);
        
        UserEntity user2 = new UserEntity();
        user2.setName("Bob");
        user2.setAge(25);
        
        return Flux.just(user1, user2);
    }

    // 6. SSE (Server-Sent Events) 流式传输
    @GetMapping(value = "/see", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> see() {
        return Flux.range(1, 10)
                .map(i -> "Flux Item " + i)
                .delayElements(Duration.ofMillis(500));
    }
}
```

**关键知识点：**
- `@RestController`：标记为 REST 控制器
- `@RequestMapping("/api")`：统一 API 前缀
- `Mono<T>`：表示 0 或 1 个元素的异步序列
- `Flux<T>`：表示 0 到 N 个元素的异步序列
- `MediaType.TEXT_EVENT_STREAM_VALUE`：SSE 流式传输
- `.delayElements()`：延迟发射元素，模拟实时数据流

## 5.2 UserController - 用户管理 REST API

```java
package com.wzh.demo.controller;

import com.wzh.demo.model.UserEntity;
import com.wzh.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 查询所有用户
    @GetMapping
    public Flux<UserEntity> findAll() {
        return userService.findAll();
    }

    // 根据ID查询单个用户
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserEntity>> findById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 新增用户
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserEntity> save(@RequestBody UserEntity user) {
        return userService.save(user);
    }

    // 更新用户
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserEntity>> update(@PathVariable Long id, @RequestBody UserEntity user) {
        return userService.update(id, user)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 删除用户
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteById(@PathVariable Long id) {
        return userService.deleteById(id);
    }

    // 查询用户手机号（map 转换示例）
    @GetMapping("/{id}/phone")
    public Mono<String> getPhoneById(@PathVariable Long id) {
        return userService.findById(id)
                .map(UserEntity::getPhone)
                .defaultIfEmpty("未知手机号");
    }

    // SSE 流式传输用户列表
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserEntity> streamUsers() {
        return userService.findAll()
                .delayElements(Duration.ofMillis(500));
    }
}
```

**关键知识点：**
- `@RequestBody`：接收 JSON 请求体
- `@PathVariable`：接收路径参数
- `ResponseEntity`：自定义 HTTP 响应状态码
- `@ResponseStatus`：设置响应状态码
- `.defaultIfEmpty()`：处理空值情况
- `.map()`：转换数据格式

# 六、REST 接口调用（客户端）

## 6.1 在线接口测试页面

项目提供了完整的在线接口测试页面，访问 http://localhost:8080/api-demo 即可使用。

**页面功能：**
- ✅ 展示所有 REST 接口的详细说明
- ✅ 可配置请求参数和请求体
- ✅ 一键执行请求并查看响应
- ✅ 实时显示 HTTP 状态码
- ✅ 格式化展示 JSON 响应数据
- ✅ 支持 SSE 流式数据实时接收

**测试接口分类：**

### Mono 示例
- `GET /api/hello?key={name}` - 简单字符串返回
- `GET /api/helloMono/{name}` - 路径参数示例
- `GET /api/helloMap` - 随机生成 Map 数据

### Flux 示例
- `GET /api/helloFlux` - 返回 Flux<String>
- `GET /api/helloFlux2` - 返回 Flux<UserEntity>
- `GET /api/see` - SSE 流式传输（实时接收数据）

### 用户管理 API
- `GET /api/users` - 查询所有用户
- `GET /api/users/{id}` - 根据ID查询用户
- `POST /api/users` - 新增用户
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户
- `GET /api/users/{id}/phone` - 查询用户手机号

## 6.2 WebClient 编程式调用

除了在线测试页面，还可以使用 WebClient 进行编程式调用：

```java
package com.wzh.demo.client;

import com.wzh.demo.model.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserClient {
    
    private final WebClient webClient;
    
    /**
     * 查询所有用户
     */
    public Flux<UserEntity> findAllUsers() {
        return webClient.get()
                .uri("/api/users")
                .retrieve()
                .bodyToFlux(UserEntity.class);
    }
    
    /**
     * 根据ID查询用户
     */
    public Mono<UserEntity> findUserById(Long id) {
        return webClient.get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .bodyToMono(UserEntity.class);
    }
    
    /**
     * 保存用户
     */
    public Mono<UserEntity> saveUser(UserEntity user) {
        return webClient.post()
                .uri("/api/users")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(UserEntity.class);
    }
    
    /**
     * flatMap 结合 WebClient 异步调用
     * 先保存用户，再查询用户，最后获取手机号
     */
    public Mono<String> saveUserAndGetPhone(UserEntity user) {
        return saveUser(user)
                .flatMap(savedUser -> findUserById(savedUser.getId()))
                .map(UserEntity::getPhone);
    }
    
    /**
     * 批量查询用户（Flux + flatMap 并发调用）
     */
    public Flux<UserEntity> batchFindUsers(Long... ids) {
        return Flux.fromArray(ids)
                .flatMap(this::findUserById);
    }
    
    /**
     * 错误处理
     */
    public Mono<UserEntity> findUserWithHandling(Long id) {
        return webClient.get()
                .uri("/api/users/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> 
                    Mono.error(new RuntimeException("用户不存在: " + id)))
                .onStatus(HttpStatus::is5xxServerError, response -> 
                    Mono.error(new RuntimeException("服务器错误")))
                .bodyToMono(UserEntity.class);
    }
}
```

**WebClient 配置：**

```java
package com.wzh.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }
}
```

**关键知识点：**
- `WebClient`：响应式 HTTP 客户端
- `.get()/.post()/.put()/.delete()`：HTTP 方法
- `.uri()`：设置请求 URI
- `.bodyValue()`：设置请求体
- `.retrieve()`：执行请求并获取响应
- `.bodyToMono()/.bodyToFlux()`：转换响应体
- `.onStatus()`：错误处理
- `flatMap`：链式异步调用
- `Flux.fromArray().flatMap()`：并发批量调用

## 6.3 使用 curl 命令测试

```bash
# 查询所有用户
curl http://localhost:8080/api/users

# 根据ID查询用户
curl http://localhost:8080/api/users/1

# 新增用户
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"测试用户","age":25,"phone":"13900139000","email":"test@example.com","active":true}'

# 更新用户
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"更新后的用户","age":26,"phone":"13900139001","email":"updated@example.com","active":true}'

# 删除用户
curl -X DELETE http://localhost:8080/api/users/1

# 查询用户手机号
curl http://localhost:8080/api/users/1/phone

# SSE 流式接收（会持续输出数据）
curl http://localhost:8080/api/see
```

## 6.4 使用 Postman 测试

1. **创建 Collection**：命名为 "WebFlux API"
2. **添加请求**：
   - GET http://localhost:8080/api/users
   - GET http://localhost:8080/api/users/1
   - POST http://localhost:8080/api/users（Body 选择 raw JSON）
   - PUT http://localhost:8080/api/users/1
   - DELETE http://localhost:8080/api/users/1
3. **设置 Headers**：`Content-Type: application/json`
4. **发送请求并查看响应**

**Postman 高级功能：**
- 环境变量：设置 `base_url=http://localhost:8080`
- 预请求脚本：自动生成测试数据
- 测试脚本：验证响应状态码和数据格式
- Collection Runner：批量执行所有接口测试

## 6.5 前端使用 Axios 调用

Axios 是一个基于 Promise 的 HTTP 客户端，适用于浏览器和 Node.js。相比原生 Fetch API，Axios 提供了更简洁的 API 和更好的错误处理。

### 6.5.1 引入 Axios

**方式1：CDN 引入（推荐用于简单项目）**

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Axios 示例</title>
    <!-- 引入 Axios -->
    <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
</head>
<body>
    <div id="app"></div>
    <script src="app.js"></script>
</body>
</html>
```

**方式2：npm 安装（推荐用于大型项目）**

```bash
npm install axios
```

```javascript
import axios from 'axios';
```

### 6.5.2 Axios 基本配置

```javascript
// 配置 Axios 默认值
axios.defaults.baseURL = 'http://localhost:8080';
axios.defaults.headers.common['Content-Type'] = 'application/json';
axios.defaults.timeout = 10000; // 超时时间 10 秒

// 创建 Axios 实例（推荐）
const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});
```

### 6.5.3 发送 GET 请求

```javascript
// 1. 查询所有用户
async function getAllUsers() {
    try {
        const response = await axios.get('/api/users');
        console.log('用户列表:', response.data);
        return response.data;
    } catch (error) {
        console.error('请求失败:', error.message);
        throw error;
    }
}

// 2. 根据 ID 查询用户
async function getUserById(id) {
    try {
        const response = await axios.get(`/api/users/${id}`);
        console.log('用户信息:', response.data);
        return response.data;
    } catch (error) {
        if (error.response) {
            // 服务器返回了错误状态码
            console.error('服务器错误:', error.response.status, error.response.data);
        } else if (error.request) {
            // 请求已发出但没有收到响应
            console.error('网络错误:', error.request);
        } else {
            // 其他错误
            console.error('请求错误:', error.message);
        }
        throw error;
    }
}

// 3. 查询用户手机号
async function getUserPhone(id) {
    try {
        const response = await axios.get(`/api/users/${id}/phone`);
        console.log('手机号:', response.data);
        return response.data;
    } catch (error) {
        console.error('请求失败:', error.message);
        throw error;
    }
}
```

### 6.5.4 发送 POST 请求

```javascript
// 新增用户
async function createUser(userData) {
    try {
        const response = await axios.post('/api/users', userData);
        console.log('创建成功:', response.data);
        console.log('状态码:', response.status); // 201
        return response.data;
    } catch (error) {
        console.error('创建失败:', error.response?.data || error.message);
        throw error;
    }
}

// 使用示例
const newUser = {
    name: '测试用户',
    age: 25,
    phone: '13900139000',
    email: 'test@example.com',
    active: true
};

createUser(newUser);
```

### 6.5.5 发送 PUT 请求

```javascript
// 更新用户
async function updateUser(id, userData) {
    try {
        const response = await axios.put(`/api/users/${id}`, userData);
        console.log('更新成功:', response.data);
        return response.data;
    } catch (error) {
        console.error('更新失败:', error.response?.data || error.message);
        throw error;
    }
}

// 使用示例
const updatedUser = {
    name: '更新后的用户',
    age: 26,
    phone: '13900139001',
    email: 'updated@example.com',
    active: true
};

updateUser(1, updatedUser);
```

### 6.5.6 发送 DELETE 请求

```javascript
// 删除用户
async function deleteUser(id) {
    try {
        const response = await axios.delete(`/api/users/${id}`);
        console.log('删除成功，状态码:', response.status); // 204
        return true;
    } catch (error) {
        console.error('删除失败:', error.response?.data || error.message);
        throw error;
    }
}

// 使用示例
deleteUser(1);
```

### 6.5.7 完整的封装示例

```javascript
// api.js - API 封装模块

// 创建 Axios 实例
const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
});

// 请求拦截器
apiClient.interceptors.request.use(
    config => {
        console.log('发送请求:', config.method.toUpperCase(), config.url);
        // 可以在这里添加 token
        // const token = localStorage.getItem('token');
        // if (token) {
        //     config.headers.Authorization = `Bearer ${token}`;
        // }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器
apiClient.interceptors.response.use(
    response => {
        console.log('响应成功:', response.status);
        return response.data;
    },
    error => {
        if (error.response) {
            console.error('服务器错误:', error.response.status, error.response.data);
        } else if (error.request) {
            console.error('网络错误: 无法连接到服务器');
        } else {
            console.error('请求错误:', error.message);
        }
        return Promise.reject(error);
    }
);

// 用户 API
export const userAPI = {
    // 查询所有用户
    getAll: () => apiClient.get('/api/users'),
    
    // 根据 ID 查询用户
    getById: (id) => apiClient.get(`/api/users/${id}`),
    
    // 新增用户
    create: (userData) => apiClient.post('/api/users', userData),
    
    // 更新用户
    update: (id, userData) => apiClient.put(`/api/users/${id}`, userData),
    
    // 删除用户
    delete: (id) => apiClient.delete(`/api/users/${id}`),
    
    // 查询用户手机号
    getPhone: (id) => apiClient.get(`/api/users/${id}/phone`)
};

// Hello API
export const helloAPI = {
    hello: (key) => apiClient.get('/api/hello', { params: { key } }),
    helloMono: (name) => apiClient.get(`/api/helloMono/${name}`),
    helloMap: () => apiClient.get('/api/helloMap'),
    helloFlux: () => apiClient.get('/api/helloFlux'),
    helloFlux2: () => apiClient.get('/api/helloFlux2')
};
```

**使用封装后的 API：**

```javascript
import { userAPI, helloAPI } from './api.js';

// 查询所有用户
userAPI.getAll()
    .then(users => {
        console.log('用户列表:', users);
        // 渲染到页面
        renderUsers(users);
    })
    .catch(error => {
        console.error('获取用户列表失败:', error);
        showError('加载失败，请重试');
    });

// 新增用户
userAPI.create({
    name: '新用户',
    age: 20,
    phone: '13900000000',
    email: 'new@example.com',
    active: true
})
.then(user => {
    console.log('创建成功:', user);
    showSuccess('用户创建成功');
    // 刷新列表
    loadUserList();
})
.catch(error => {
    console.error('创建失败:', error);
    showError('创建失败，请检查输入');
});

// 链式调用：先保存用户，再查询手机号
helloAPI.helloMono('World')
    .then(message => {
        console.log(message); // "Hello Mono World"
        return userAPI.getById(1);
    })
    .then(user => {
        console.log('用户:', user);
        return userAPI.getPhone(user.id);
    })
    .then(phone => {
        console.log('手机号:', phone);
    })
    .catch(error => {
        console.error('操作失败:', error);
    });
```

### 6.5.8 Axios vs Fetch 对比

| 特性 | Axios | Fetch |
|------|-------|-------|
| **安装** | 需要安装或 CDN 引入 | 浏览器原生支持 |
| **语法** | 更简洁直观 | 较繁琐 |
| **自动 JSON 转换** | ✅ 自动 | ❌ 需手动 `.json()` |
| **错误处理** | ✅ 完善的错误对象 | ❌ 只有网络错误才 reject |
| **请求拦截器** | ✅ 支持 | ❌ 不支持 |
| **响应拦截器** | ✅ 支持 | ❌ 不支持 |
| **取消请求** | ✅ `CancelToken` | ✅ `AbortController` |
| **浏览器兼容** | 需要 polyfill（IE） | 现代浏览器支持 |
| **文件大小** | ~13KB (gzipped) | 0KB (原生) |

**推荐使用场景：**
- ✅ **小型项目**：直接使用 Fetch API，无需额外依赖
- ✅ **中大型项目**：使用 Axios，更好的错误处理和拦截器支持
- ✅ **需要请求/响应拦截**：必须使用 Axios
- ✅ **需要自动 JSON 转换**：Axios 更方便

### 6.5.9 实战示例：接口测试页面

本项目中的 `/api-demo` 页面就是使用 Axios 实现的完整示例：

```javascript
// 通用请求函数（使用 Axios）
async function makeRequest(url, method = 'GET', body = null) {
    try {
        const config = {
            method: method.toLowerCase(),
            url: url
        };

        if (body && (method === 'POST' || method === 'PUT')) {
            config.data = body;
        }

        const response = await axios(config);
        
        return {
            success: true,
            statusCode: response.status,
            data: response.data
        };
    } catch (error) {
        if (error.response) {
            // 服务器返回了错误状态码
            return {
                success: false,
                statusCode: error.response.status,
                data: error.response.data,
                error: error.response.data?.message || error.message
            };
        } else if (error.request) {
            // 请求已发出但没有收到响应
            return {
                success: false,
                statusCode: 0,
                error: '网络错误，无法连接到服务器'
            };
        } else {
            // 其他错误
            return {
                success: false,
                statusCode: 0,
                error: error.message
            };
        }
    }
}

// 使用示例
async function callGetAllUsers() {
    showLoading('getallusers-response');
    const result = await makeRequest('/api/users');
    showResponse('getallusers-response', result);
}

async function callCreateUser() {
    const bodyText = document.getElementById('postuser-body').value;
    showLoading('createuser-response');
    try {
        const body = JSON.parse(bodyText);
        const result = await makeRequest('/api/users', 'POST', body);
        showResponse('createuser-response', result);
    } catch (e) {
        showResponse('createuser-response', {
            success: false,
            statusCode: 0,
            error: 'JSON 格式错误: ' + e.message
        });
    }
}
```

**访问在线演示：** http://localhost:8080/api-demo
- 测试脚本：验证响应状态码和数据格式
- Collection Runner：批量执行所有接口测试

## 5.1 用户服务（UserService）

```java
package com.example.webfluxdemo.service;

import com.example.webfluxdemo.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserEntity> findById(Long id);
    Mono<UserEntity> save(UserEntity user);
    Mono<UserEntity> update(Long id, UserEntity user);
    Mono<Void> deleteById(Long id);
    Page<UserEntity> findUsersWithPage(Pageable pageable);
    Mono<UserEntity> toggleUserStatus(Long id);
}
```

## 5.2 角色服务（RoleService）

```java
package com.example.webfluxdemo.service;

import com.example.webfluxdemo.model.RoleEntity;
import com.example.webfluxdemo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
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

    public Mono<RoleEntity> save(RoleEntity role) {
        return Mono.fromCallable(() -> roleRepository.save(role))
                .flatMapMany(Flux::just).next();
    }

    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> roleRepository.deleteById(id));
    }
}
```

# 六、安全配置（Spring WebFlux Security）

## 6.1 安全配置类

**⚠️ 重要：WebFlux 项目必须使用响应式 Security 配置！**

```java
package com.example.webfluxdemo.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity  // 注意：不是 @EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(auth -> auth  // 注意：不是 authorizeHttpRequests
                .pathMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()  // 注意：不是 requestMatchers
                .pathMatchers("/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated())  // 注意：不是 anyRequest
            .formLogin(form -> form
                .loginPage("/login"))
            .logout(logout -> logout
                .logoutUrl("/logout"));

        return http.build();
    }
}
```

**关键区别：**
- `@EnableWebFluxSecurity` vs `@EnableWebSecurity`
- `ServerHttpSecurity` vs `HttpSecurity`
- `SecurityWebFilterChain` vs `SecurityFilterChain`
- `authorizeExchange()` vs `authorizeHttpRequests()`
- `pathMatchers()` vs `requestMatchers()`
- `anyExchange()` vs `anyRequest()`
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin/users", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
```

## 6.2 自定义用户详情服务

**⚠️ 重要：WebFlux 必须使用 ReactiveUserDetailsService！**

```java
package com.example.webfluxdemo.service;

import com.example.webfluxdemo.model.UserEntity;
import com.example.webfluxdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;  // 注意：不是 UserDetailsService
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;  // 导入 Mono
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {  // 注意：返回 Mono<UserDetails>
        return Mono.fromCallable(() -> userRepository.findByName(username)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + username)))
                .map(userEntity -> {
                    if (!userEntity.getActive()) {
                        throw new RuntimeException("用户已被屏蔽: " + username);
                    }

                    // 获取用户的所有角色
                    List<SimpleGrantedAuthority> authorities = userEntity.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                            .collect(Collectors.toList());

                    return (UserDetails) User.builder()
                            .username(userEntity.getName())
                            .password("") // 密码为空，实际项目中应该存储加密密码
                            .authorities(authorities)
                            .build();
                });
    }
}
```

**关键区别：**
- 实现 `ReactiveUserDetailsService` vs `UserDetailsService`
- 方法名 `findByUsername()` vs `loadUserByUsername()`
- 返回类型 `Mono<UserDetails>` vs `UserDetails`
- 使用 `Mono.fromCallable()` 包装阻塞的 JPA 调用

# 七、控制器层（Controller）

## 7.1 登录控制器

```java
package com.example.webfluxdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(Model model) {
        return "login";
    }
}
```

## 7.2 后台管理控制器

```java
package com.example.webfluxdemo.controller;

import com.example.webfluxdemo.model.RoleEntity;
import com.example.webfluxdemo.model.UserEntity;
import com.example.webfluxdemo.service.RoleService;
import com.example.webfluxdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    // 用户列表页面（分页）
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<UserEntity> userPage = userService.findUsersWithPage(pageable);
        
        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        
        return "admin/user-list";
    }

    // 显示新增用户表单
    @GetMapping("/users/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserEntity());
        model.addAttribute("isEdit", false);
        model.addAttribute("allRoles", roleService.findAllRoles());
        return "admin/user-form";
    }

    // 保存新用户
    @PostMapping("/users/save")
    public Mono<String> saveUser(@ModelAttribute UserEntity user) {
        return userService.save(user)
                .then(Mono.just("redirect:/admin/users"));
    }

    // 显示编辑用户表单
    @GetMapping("/users/edit/{id}")
    public Mono<String> showEditForm(@PathVariable Long id, Model model) {
        return userService.findById(id)
                .map(user -> {
                    model.addAttribute("user", user);
                    model.addAttribute("isEdit", true);
                    model.addAttribute("allRoles", roleService.findAllRoles());
                    return "admin/user-form";
                })
                .defaultIfEmpty("redirect:/admin/users");
    }

    // 更新用户
    @PostMapping("/users/update/{id}")
    public Mono<String> updateUser(@PathVariable Long id, @ModelAttribute UserEntity user) {
        return userService.update(id, user)
                .then(Mono.just("redirect:/admin/users"));
    }

    // 删除用户
    @PostMapping("/users/delete/{id}")
    public Mono<String> deleteUser(@PathVariable Long id) {
        return userService.deleteById(id)
                .then(Mono.just("redirect:/admin/users"));
    }

    // 屏蔽/激活用户
    @PostMapping("/users/toggle/{id}")
    public Mono<String> toggleUserStatus(@PathVariable Long id) {
        return userService.toggleUserStatus(id)
                .then(Mono.just("redirect:/admin/users"));
    }
    
    // ==================== 角色管理 ====================
    
    // 角色列表页面
    @GetMapping("/roles")
    public String listRoles(Model model) {
        model.addAttribute("roles", roleService.findAllRoles());
        return "admin/role-list";
    }
    
    // 显示新增角色表单
    @GetMapping("/roles/new")
    public String showCreateRoleForm(Model model) {
        model.addAttribute("role", new RoleEntity());
        model.addAttribute("isEdit", false);
        return "admin/role-form";
    }
    
    // 保存新角色
    @PostMapping("/roles/save")
    public Mono<String> saveRole(@ModelAttribute RoleEntity role) {
        return roleService.save(role)
                .then(Mono.just("redirect:/admin/roles"));
    }
    
    // 显示编辑角色表单
    @GetMapping("/roles/edit/{id}")
    public Mono<String> showEditRoleForm(@PathVariable Long id, Model model) {
        return roleService.findById(id)
                .map(role -> {
                    model.addAttribute("role", role);
                    model.addAttribute("isEdit", true);
                    return "admin/role-form";
                })
                .defaultIfEmpty("redirect:/admin/roles");
    }
    
    // 更新角色
    @PostMapping("/roles/update/{id}")
    public Mono<String> updateRole(@PathVariable Long id, @ModelAttribute RoleEntity role) {
        role.setId(id);
        return roleService.save(role)
                .then(Mono.just("redirect:/admin/roles"));
    }
    
    // 删除角色
    @PostMapping("/roles/delete/{id}")
    public Mono<String> deleteRole(@PathVariable Long id) {
        return roleService.deleteById(id)
                .then(Mono.just("redirect:/admin/roles"));
    }
}
```

# 八、前端页面（Thymeleaf 模板）

## 8.1 登录页面（login.html）

创建 `src/main/resources/templates/login.html`，包含美观的渐变背景登录界面。

## 8.2 用户列表页面（user-list.html）

创建 `src/main/resources/templates/admin/user-list.html`，实现：
- 分页显示用户列表
- 显示用户的多角色标签
- 用户状态显示（正常/已屏蔽）
- 操作按钮（编辑、屏蔽/激活、删除）
- 分页导航

## 8.3 用户表单页面（user-form.html）

创建 `src/main/resources/templates/admin/user-form.html`，实现：
- 新增/编辑用户表单
- 多角色复选框选择
- 表单验证
- 状态选择

## 8.4 角色列表页面（role-list.html）

创建 `src/main/resources/templates/admin/role-list.html`，实现：
- 角色列表展示
- 角色描述显示
- 编辑和删除操作

## 8.5 角色表单页面（role-form.html）

创建 `src/main/resources/templates/admin/role-form.html`，实现：
- 新增/编辑角色表单
- 角色名称和描述输入

# 九、数据初始化

```java
package com.example.webfluxdemo.config;

import com.example.webfluxdemo.model.RoleEntity;
import com.example.webfluxdemo.model.UserEntity;
import com.example.webfluxdemo.repository.RoleRepository;
import com.example.webfluxdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // 创建初始角色
        if (roleRepository.count() == 0) {
            RoleEntity adminRole = new RoleEntity(null, "ADMIN", "系统管理员，拥有所有权限");
            RoleEntity userRole = new RoleEntity(null, "USER", "普通用户，拥有基本权限");
            RoleEntity managerRole = new RoleEntity(null, "MANAGER", "经理，拥有管理权限");
            
            roleRepository.save(adminRole);
            roleRepository.save(userRole);
            roleRepository.save(managerRole);
        }
        
        // 创建初始用户
        if (userRepository.count() == 0) {
            RoleEntity adminRole = roleRepository.findByName("ADMIN").orElse(null);
            RoleEntity userRole = roleRepository.findByName("USER").orElse(null);
            
            // 创建管理员用户
            Set<RoleEntity> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            UserEntity admin = new UserEntity(null, "admin", 30, "13800000000", 
                                            "admin@example.com", true, adminRoles);
            
            // 创建普通用户
            Set<RoleEntity> userRoles = new HashSet<>();
            userRoles.add(userRole);
            UserEntity user1 = new UserEntity(null, "张三", 20, "13800138000", 
                                             "zhangsan@example.com", true, userRoles);
            
            userRepository.save(admin);
            userRepository.save(user1);
        }
    }
}
```

# 十、功能特性总结

## 10.1 核心功能

✅ **登录认证**：Spring Security 实现用户登录和权限控制  
✅ **角色管理**：支持多角色分配和基于角色的访问控制（RBAC）  
✅ **用户管理**：完整的 CRUD 操作，支持分页显示  
✅ **多对多关系**：用户可以拥有多个角色  
✅ **用户状态管理**：支持屏蔽/激活用户  
✅ **美观界面**：Thymeleaf 模板引擎打造的现代化后台  
✅ **Netty 容器**：使用响应式 Netty 服务器，高性能非阻塞 I/O

## 10.2 技术亮点

- **响应式编程**：使用 Mono/Flux 处理异步操作
- **JPA 多对多映射**：用户-角色关系通过中间表实现
- **分页查询**：Spring Data JPA 原生支持
- **安全认证**：基于角色的权限控制（WebFlux Security）
- **数据初始化**：CommandLineRunner 自动创建初始数据
- **懒加载优化**：手动实现 equals/hashCode 避免 LazyInitializationException

## 10.3 使用说明

1. 启动项目后访问 http://localhost:8080/login
2. 使用测试账号登录：
   - **管理员**：用户名 `admin`，密码任意 → 进入后台管理页面
   - **普通用户**：用户名 `张三`、`李四`、`王五`，密码任意 → 进入欢迎页面
3. 进入系统后，可以：
   - 管理员：管理用户（增删改查、分页、屏蔽）、管理角色（增删改查）
   - 普通用户：查看 Spring WebFlux 完整教程内容

# 十一、常见问题

## Q1: 如何实现真正的密码验证？

A: 在 CustomUserDetailsService 中，将空字符串替换为数据库中存储的加密密码，Spring Security 会自动使用 PasswordEncoder 进行验证。

## Q2: 多对多关系如何避免循环引用？

A: 在 RoleEntity 中添加不包含 users 集合的构造函数，避免 JSON 序列化和 Lombok 生成方法时的循环引用问题。

## Q3: 如何实现更细粒度的权限控制？

A: 可以使用 `@PreAuthorize` 注解在方法级别进行权限控制，例如：
```java
@PreAuthorize("hasRole('ADMIN')")
public Mono<String> deleteUser(@PathVariable Long id) {
    // ...
}
```

## Q4: 分页参数如何传递？

A: 通过 URL 参数传递，例如：`/admin/users?page=0&size=10`

## Q5: WebFlux 项目为什么不能使用 Tomcat？

A: WebFlux 项目可以使用 Tomcat，但推荐使用 Netty 容器。Netty 是响应式的非阻塞 I/O 服务器，与 WebFlux 的响应式编程模型更匹配，性能更好。如果使用 Tomcat，需要额外配置且无法发挥 WebFlux 的全部优势。

## Q6: 为什么使用 @EnableWebFluxSecurity 而不是 @EnableWebSecurity？

A: `@EnableWebSecurity` 是用于 Servlet 应用的，而 `@EnableWebFluxSecurity` 是用于响应式 WebFlux 应用的。两者使用的安全过滤器链不同：
- Servlet: `SecurityFilterChain` + `HttpSecurity`
- WebFlux: `SecurityWebFilterChain` + `ServerHttpSecurity`

混用会导致 Bean 冲突和启动失败。

## Q7: 为什么 CustomUserDetailsService 要返回 Mono<UserDetails>？

A: WebFlux 是响应式的，所有操作都应该返回 Mono 或 Flux。即使 JPA 是阻塞的，我们也通过 `Mono.fromCallable()` 将其包装为响应式类型，在独立的线程池中执行，避免阻塞主事件循环。

## Q8: 如何实现不同角色登录后跳转到不同页面？

A: 通过自定义 `ServerAuthenticationSuccessHandler` 实现：
```java
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
```

## Q9: 为什么登录页面会显示“登录失败”提示？

A: 这是因为 Thymeleaf 的 `th:if="${param.error}"` 在参数存在时就会显示。应该改为检查参数是否为空且有值：
```html
<div th:if="${param.error != null and param.error.size() > 0}" class="alert alert-error">
  用户名或密码错误
</div>
```

# 十二、扩展学习

1. **集成 JWT**：实现无状态认证
2. **Redis 缓存**：缓存用户和角色信息
3. **审计日志**：记录用户操作日志
4. **文件上传**：用户头像上传功能
5. **数据导出**：Excel 导出用户列表
6. **消息通知**：站内信或邮件通知

---

**祝学习愉快！** 🎉

> **提示**：本教程配套完整代码示例，请参考项目中的实际实现。
