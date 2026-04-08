package com.wzh.demo.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

import com.wzh.demo.model.RoleEntity;
import com.wzh.demo.model.UserEntity;
import com.wzh.demo.service.RoleService;
import com.wzh.demo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final UserService userService;
  private final RoleService roleService;
  private final com.wzh.demo.repository.RoleRepository roleRepository;

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
  public Mono<String> showCreateForm(Model model) {
    return Mono.fromCallable(() -> {
      model.addAttribute("user", new UserEntity());
      model.addAttribute("isEdit", false);
      model.addAttribute("allRoles", roleService.findAllRoles());
      return "admin/user-form";
    });
  }

  /**
   * 保存新用户
   * 
   * 注意：在 WebFlux 中，使用 ServerWebExchange.getFormData() 异步获取表单数据，
   * 这样可以正确处理同名多值参数（如多个 roleIds）。
   * 
   * @param exchange WebFlux 交换对象，用于获取请求数据
   * @return 重定向到用户列表页面
   */
  @PostMapping("/users/save")
  public Mono<String> saveUser(ServerWebExchange exchange) {
    // 异步获取表单数据
    return exchange.getFormData()
        .flatMap(formData -> {
          log.info("保存新用户");
          log.debug("表单数据: {}", formData);

          // 从表单数据中提取用户字段
          String name = formData.getFirst("name");
          String ageStr = formData.getFirst("age");
          String phone = formData.getFirst("phone");
          String email = formData.getFirst("email");
          String activeStr = formData.getFirst("active");

          // 提取角色 IDs（支持同名多值参数）
          List<String> roleIdsList = formData.get("roleIds");
          log.info("接收到的角色 IDs: {}", roleIdsList);

          return Mono.fromCallable(() -> {
            // 验证必填字段
            if (name == null || name.isEmpty()) {
              throw new RuntimeException("姓名不能为空");
            }
            if (ageStr == null || ageStr.isEmpty()) {
              throw new RuntimeException("年龄不能为空");
            }
            if (phone == null || phone.isEmpty()) {
              throw new RuntimeException("手机号不能为空");
            }

            // 创建用户对象
            UserEntity user = new UserEntity();
            user.setName(name);
            user.setAge(Integer.parseInt(ageStr));
            user.setPhone(phone);
            user.setEmail(email);
            user.setActive(activeStr != null ? Boolean.parseBoolean(activeStr) : true);

            // 将角色 ID 字符串转换为 RoleEntity 对象集合
            Set<RoleEntity> roleEntities = new HashSet<>();
            if (roleIdsList != null && !roleIdsList.isEmpty()) {
              for (String roleIdStr : roleIdsList) {
                try {
                  Long roleId = Long.parseLong(roleIdStr);
                  RoleEntity role = roleRepository.findById(roleId)
                      .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
                  roleEntities.add(role);
                  log.info("添加角色: {} (ID: {})", role.getName(), roleId);
                } catch (NumberFormatException e) {
                  throw new RuntimeException("无效的角色 ID: " + roleIdStr);
                }
              }
            }
            user.setRoles(roleEntities);
            log.info("设置后的角色数量: {}", roleEntities.size());

            return user;
          })
              .flatMap(newUser -> userService.save(newUser))
              .then(Mono.just("redirect:/admin/users"))
              .onErrorResume(e -> {
                log.error("保存用户失败: {}", e.getMessage(), e);
                return Mono.just("redirect:/admin/users/new?error=" + e.getMessage());
              });
        });
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

  /**
   * 更新用户信息
   * 
   * 注意：在 WebFlux 中，不能同时使用 @ModelAttribute 和 @RequestParam 接收表单数据，
   * 需要使用 ServerWebExchange.getFormData() 异步获取表单数据。
   * 这样可以正确处理同名多值参数（如多个 roleIds）。
   * 
   * @param id       用户ID
   * @param exchange WebFlux 交换对象，用于获取请求数据
   * @return 重定向到用户列表页面
   */
  @PostMapping("/users/update/{id}")
  public Mono<String> updateUser(@PathVariable Long id, ServerWebExchange exchange) {
    // 异步获取表单数据（返回 Mono<MultiValueMap<String, String>>）
    return exchange.getFormData()
        .flatMap(formData -> {
          log.info("更新用户 ID: {}", id);
          log.debug("表单数据 keys: {}", formData.keySet());
          log.debug("表单数据: {}", formData);

          // 从表单数据中提取用户基本字段（getFirst 获取单个值）
          String name = formData.getFirst("name");
          String ageStr = formData.getFirst("age");
          String phone = formData.getFirst("phone");
          String email = formData.getFirst("email");
          String activeStr = formData.getFirst("active");

          // 提取角色 IDs（get 返回 List<String>，支持同名多值参数）
          List<String> roleIdsList = formData.get("roleIds");
          log.info("角色 IDs 列表: {}", roleIdsList);

          // 在阻塞线程中处理业务逻辑
          return Mono.fromCallable(() -> {
            // 验证必填字段
            if (name == null || name.isEmpty()) {
              throw new RuntimeException("姓名不能为空");
            }
            if (ageStr == null || ageStr.isEmpty()) {
              throw new RuntimeException("年龄不能为空");
            }
            if (phone == null || phone.isEmpty()) {
              throw new RuntimeException("手机号不能为空");
            }

            // 创建并填充用户对象
            UserEntity user = new UserEntity();
            user.setName(name);
            user.setAge(Integer.parseInt(ageStr));
            user.setPhone(phone);
            user.setEmail(email);
            user.setActive(activeStr != null ? Boolean.parseBoolean(activeStr) : true);

            // 将角色 ID 字符串转换为 RoleEntity 对象集合
            Set<RoleEntity> roleEntities = new HashSet<>();
            if (roleIdsList != null && !roleIdsList.isEmpty()) {
              for (String roleIdStr : roleIdsList) {
                try {
                  Long roleId = Long.parseLong(roleIdStr);
                  // 从数据库查询角色对象
                  RoleEntity role = roleRepository.findById(roleId)
                      .orElseThrow(() -> new RuntimeException("角色不存在: " + roleId));
                  roleEntities.add(role);
                } catch (NumberFormatException e) {
                  throw new RuntimeException("无效的角色 ID: " + roleIdStr);
                }
              }
            }
            // 设置用户角色关系
            user.setRoles(roleEntities);
            log.info("设置后的角色数量: {}", roleEntities.size());

            return user;
          })
              // 调用服务层更新用户（包括角色关系）
              .flatMap(updatedUser -> userService.update(id, updatedUser))
              // 更新成功后重定向到用户列表
              .then(Mono.just("redirect:/admin/users"))
              // 异常处理：重定向回编辑页面并显示错误信息
              .onErrorResume(e -> {
                log.error("更新用户失败: {}", e.getMessage(), e);
                return Mono.just("redirect:/admin/users/edit/" + id + "?error=" + e.getMessage());
              });
        });
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
        .then(Mono.just("redirect:/admin/roles"))
        .onErrorResume(e -> Mono.just("redirect:/admin/roles/new?error=" + e.getMessage()));
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
