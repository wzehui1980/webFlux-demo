package com.wzh.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class WelcomeController {

  @GetMapping("/welcome")
  public Mono<String> welcome(Authentication authentication, Model model) {
    if (authentication != null) {
      String username = authentication.getName();
      String roles = authentication.getAuthorities().stream()
          .map(authority -> authority.getAuthority().replace("ROLE_", ""))
          .reduce((r1, r2) -> r1 + ", " + r2)
          .orElse("USER");

      model.addAttribute("username", username);
      model.addAttribute("roles", roles);
    }

    return Mono.just("welcome");
  }
}
