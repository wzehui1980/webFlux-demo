package com.wzh.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Server;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * H2 Console 配置
 * 
 * 由于 WebFlux 基于 Netty（非 Servlet 容器），无法直接使用 /h2-console 路径。
 * 此组件手动启动 H2 Web Console 服务器在独立端口上。
 * 
 * 访问地址：http://localhost:8082
 * JDBC URL: jdbc:h2:mem:testdb
 * 用户名: sa
 * 密码: (留空)
 */
@Slf4j
@Component
public class H2ConsoleConfig {

  private Server webServer;
  private Server tcpServer;

  /**
   * 应用启动时启动 H2 Console 服务器
   */
  @org.springframework.context.event.EventListener(ContextRefreshedEvent.class)
  public void start() throws SQLException {
    log.info("正在启动 H2 Console 服务器...");

    // 启动 Web Console 服务器（端口 8082）
    this.webServer = Server.createWebServer("-webPort", "8082", "-tcpAllowOthers")
        .start();

    // 启动 TCP 服务器（端口 9092），允许远程连接
    this.tcpServer = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers")
        .start();

    log.info("H2 Console 已启动 - Web: http://localhost:8082, TCP: localhost:9092");
  }

  /**
   * 应用关闭时停止 H2 Console 服务器
   */
  @org.springframework.context.event.EventListener(ContextClosedEvent.class)
  public void stop() {
    log.info("正在停止 H2 Console 服务器...");

    if (this.webServer != null) {
      this.webServer.stop();
    }

    if (this.tcpServer != null) {
      this.tcpServer.stop();
    }

    log.info("H2 Console 服务器已停止");
  }
}
