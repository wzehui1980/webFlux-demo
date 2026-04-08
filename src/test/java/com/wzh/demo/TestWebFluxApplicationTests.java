/*
 * @Author: berheley berheley@foxmail.com
 * @Date: 2026-04-02 16:39:36
 * @LastEditors: berheley berheley@foxmail.com
 * @LastEditTime: 2026-04-08 14:28:29
 * @FilePath: \testWebFlux\src\test\java\com\wzh\demo\TestWebFluxApplicationTests.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.wzh.demo;

import com.wzh.demo.client.UserClient;
import com.wzh.demo.model.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class TestWebFluxApplicationTests {

	@Autowired
	private UserClient userClient;

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
