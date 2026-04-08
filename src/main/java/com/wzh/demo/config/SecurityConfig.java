/*
 * @Author: berheley berheley@foxmail.com
 * @Date: 2026-04-07 14:49:54
 * @LastEditors: berheley berheley@foxmail.com
 * @LastEditTime: 2026-04-07 16:18:39
 * @FilePath: \testWebFlux\src\main\java\com\wzh\demo\config\SecurityConfig.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.wzh.demo.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    // 演示环境，任何密码都验证通过
    return new PasswordEncoder() {
      @Override
      public String encode(CharSequence rawPassword) {
        return rawPassword.toString();
      }

      @Override
      public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 任何密码都匹配
        return true;
      }
    };
  }

  @Bean
  public ServerAuthenticationSuccessHandler authenticationSuccessHandler() {
    return (webFilterExchange, authentication) -> {
      System.out.println("=== 认证成功处理器被调用 ===");
      System.out.println("=== 用户名: " + authentication.getName() + " ===");
      System.out.println("=== 权限: " + authentication.getAuthorities() + " ===");

      // 根据角色决定跳转页面
      boolean isAdmin = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      String redirectUrl = isAdmin ? "/admin/users" : "/welcome";

      System.out.println("=== 是否为管理员: " + isAdmin + ", 跳转URL: " + redirectUrl + " ===");

      webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.SEE_OTHER);
      webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create(redirectUrl));
      System.out.println("=== 设置重定向完成 ===");
      return webFilterExchange.getExchange().getResponse().setComplete();
    };
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
        .csrf(csrf -> csrf.disable()) // 禁用 CSRF 保护（演示环境）
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.disable())) // 允许 H2 Console 使用 iframe
        .authorizeExchange(auth -> auth
            .pathMatchers("/login", "/welcome", "/h2-console/**", "/css/**", "/js/**", "/images/**",
                "/api-demo", "/api-demo/**", "/api/**")
            .permitAll()
            .pathMatchers("/admin/**").hasRole("ADMIN")
            .anyExchange().authenticated())
        .formLogin(form -> form
            .loginPage("/login")
            .authenticationSuccessHandler((webFilterExchange, authentication) -> {
              System.out.println("=== 认证成功，准备重定向 ===");
              // 管理员跳转到后台，其他用户跳转到欢迎页
              boolean isAdmin = authentication.getAuthorities().stream()
                  .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
              String redirectUrl = isAdmin ? "/admin/users" : "/welcome";
              System.out.println("=== 重定向到: " + redirectUrl + " ===");

              webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.SEE_OTHER);
              webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create(redirectUrl));
              return webFilterExchange.getExchange().getResponse().setComplete();
            })
            .authenticationFailureHandler((webFilterExchange, exception) -> {
              System.out.println("=== 认证失败 ===");
              System.out.println("=== 失败原因: " + exception.getMessage() + " ===");
              System.out.println("=== 异常类型: " + exception.getClass().getName() + " ===");
              exception.printStackTrace();

              webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.SEE_OTHER);
              webFilterExchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/login?error"));
              return webFilterExchange.getExchange().getResponse().setComplete();
            }))
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler((exchange, authentication) -> {
              exchange.getExchange().getResponse().setStatusCode(HttpStatus.SEE_OTHER);
              exchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/login?logout"));
              return exchange.getExchange().getResponse().setComplete();
            }));

    return http.build();
  }
}
