package com.examreview.controller;

import com.examreview.dto.ApiResponse;
import com.examreview.dto.ChangePasswordDTO;
import com.examreview.dto.LoginRequest;
import com.examreview.dto.LoginResponse;
import com.examreview.dto.RegisterRequest;
import com.examreview.security.UserPrincipal;
import com.examreview.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证授权控制器
 * 处理用户登录、注册、获取当前用户信息、修改密码等认证相关操作
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     * 使用 Spring Security AuthenticationManager 进行用户名密码认证，认证成功后返回 JWT Token
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response, "登录成功");
    }

    /**
     * 用户注册接口
     * 校验用户名唯一性，使用 BCrypt 加密密码后入库，注册成功后返回 JWT Token
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ApiResponse.ok(response, "注册成功");
    }

    /**
     * 获取当前登录用户信息接口
     * 通过 Spring Security 上下文获取当前用户的 UserPrincipal，解析 userId 查询用户详情
     *
     * @param principal 当前登录用户的身份信息
     * @return 用户信息响应
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        LoginResponse response = authService.getCurrentUser(principal.getId());
        return ApiResponse.ok(response);
    }

    /**
     * 修改密码接口
     * 验证原密码正确性后，使用 BCrypt 加密新密码并更新
     *
     * @param dto      密码修改请求体，包含 oldPassword 和 newPassword
     * @param principal 当前登录用户的身份信息
     * @return 操作结果
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        authService.changePassword(principal.getId(), dto);
        return ApiResponse.ok(null, "密码修改成功");
    }
}
