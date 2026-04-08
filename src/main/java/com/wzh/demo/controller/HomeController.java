package com.wzh.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

  @GetMapping("/")
  public Mono<String> home(Authentication authentication) {
    if (authentication != null && authentication.isAuthenticated()) {
      // 检查是否为管理员
      boolean isAdmin = authentication.getAuthorities().stream()
          .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

      if (isAdmin) {
        return Mono.just("redirect:/admin/users");
      } else {
        return Mono.just("redirect:/welcome");
      }
    }
    return Mono.just("redirect:/login");
  }
}
