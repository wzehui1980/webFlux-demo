package com.wzh.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wzh.demo.model.UserEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class HelloController {

	@GetMapping("/hello")
	public String hello(@RequestParam(name = "key", required = false, defaultValue = "哈哈") String key) {
		return "Hello " + key;
	}

	@GetMapping("/helloMono/{name}")
	public Mono<String> helloMono(@PathVariable String name) {
		return Mono.just("Hello Mono " + name);
	}

	private final Random random = new Random();

	/**
	 * 随机生成不同类型的值
	 */
	private Object getRandomValue() {
		int type = random.nextInt(4);
		return switch (type) {
			case 0 -> UUID.randomUUID().toString().substring(0, 10); // 随机字符串
			case 1 -> random.nextInt(1000); // 随机整数
			case 2 -> random.nextDouble() * 100; // 随机小数
			case 3 -> random.nextBoolean(); // 随机布尔
			default -> "default";
		};
	}

	@GetMapping("/helloMap")
	public Mono<Map> helloMono2() {
		// 生成 5~10 个随机键值对
		Map<String, Object> map = new HashMap<>();
		int size = random.nextInt(6) + 5;
		for (int i = 0; i < size; i++) {
			String key = "key_" + UUID.randomUUID().toString().substring(0, 6);
			Object value = getRandomValue();
			map.put(key, value);
		}
		if (map.size() > 0) {
			return Mono.just(map);
		} else {
			return Mono.error(new RuntimeException("生成数据失败"));
		}

	}

	@GetMapping("/helloFlux")
	public Flux<String> helloFlux() {
		return Flux.just("Hello", "Flux");
	}

	@GetMapping("/helloFlux2")
	public Flux<UserEntity> helloFlux2() {

		UserEntity user1 = new UserEntity();
		user1.setName("Alice");
		user1.setAge(30);

		UserEntity user2 = new UserEntity();
		user2.setName("Bob");
		user2.setAge(25);

		UserEntity user3 = new UserEntity();
		user3.setName("Charlie");
		user3.setAge(35);

		return Flux.just(user1, user2, user3);

	}

	@GetMapping(value = "/see", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> see() {
		return Flux.range(1, 10).map(i -> "Flux Item " + i).delayElements(java.time.Duration.ofMillis(500));

	}

}
