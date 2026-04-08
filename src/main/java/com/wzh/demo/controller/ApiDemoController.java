package com.wzh.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * API 接口调用演示控制器
 * 提供 REST 接口在线测试页面
 */
@Slf4j
@Controller
@RequestMapping("/api-demo")
public class ApiDemoController {

  /**
   * API 演示主页
   */
  @GetMapping
  public String apiDemoPage(Model model) {
    model.addAttribute("title", "REST API 接口调用演示");
    return "api-demo/index";
  }
}
