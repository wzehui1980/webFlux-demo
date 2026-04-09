/*
 * @Author: berheley berheley@foxmail.com
 * @Date: 2026-04-02 16:39:36
 * @LastEditors: berheley berheley@foxmail.com
 * @LastEditTime: 2026-04-09 11:04:26
 * @FilePath: \testWebFlux\src\test\java\com\wzh\demo\TestWebFluxApplicationTests.java
 * @Description: WebFlux 客户端调用测试
 */
package com.wzh.demo;

import com.wzh.demo.client.UserClient;
import com.wzh.demo.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * WebFlux 客户端调用测试
 * 
 * 注意：这些测试需要应用服务器运行在 8080 端口
 * 运行测试前请先启动应用：mvn spring-boot:run
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TestWebFluxApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private UserClient userClient;

	/**
	 * 动态设置 WebClient 的 baseURL
	 */
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		// 使用固定端口进行测试
		registry.add("app.base-url", () -> "http://localhost:8080");
	}

	@Test
	void contextLoads() {
		// 基本上下文加载测试
	}

	@Test
	void testFindAllUsers() {
		Flux<UserEntity> userFlux = userClient.findAllUsers();
		StepVerifier.create(userFlux)
				.expectNextCount(3)
				.verifyComplete();
	}

	@Test
	void testFindUserById() {
		Mono<UserEntity> userMono = userClient.findUserById(1L);
		StepVerifier.create(userMono)
				.expectNextMatches(user -> user.getId().equals(1L) && user.getName().equals("张三"))
				.verifyComplete();
	}

	@Test
	void testSaveUserAndGetPhone() {
		UserEntity newUser = new UserEntity(null, "测试用户", 30, "13800138005", "test@example.com", true,
				new java.util.HashSet<>());
		Mono<String> phoneMono = userClient.saveUserAndGetPhone(newUser);
		StepVerifier.create(phoneMono)
				.expectNext("13800138005")
				.verifyComplete();
	}

	@Test
	void testBatchFindUsers() {
		Flux<UserEntity> userFlux = userClient.batchFindUsers(1L, 2L, 3L);
		StepVerifier.create(userFlux)
				.expectNextCount(3)
				.verifyComplete();
	}
}
