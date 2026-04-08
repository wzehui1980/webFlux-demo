package com.wzh.demo;

import java.io.IOException;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

/**
 *  原生REACTOR NETTY 原生api的服务器启动方式
 */
public class FluxMainApplication {
	public static void main(String[] args) throws IOException {
		HttpHandler handler = (req,resp) -> {
			System.out.println("Hello WebFlux ->  "+req.getURI());
//			resp.getHeaders().add("Content-Type", "text/plain");
			
			Mono result = resp.writeWith(Mono.just(resp.bufferFactory().wrap("Hello WebFlux".getBytes())));
			resp.setRawStatusCode(200);
			
			
			
 
			
			return result;
		};
		
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
		
		HttpServer.create().host("localhost").port(8080).handle(adapter).bindNow();
		
		System.out.println("Server Started");
		System.in.read();
		System.out.println("Server Stopped");
		
	}
}
