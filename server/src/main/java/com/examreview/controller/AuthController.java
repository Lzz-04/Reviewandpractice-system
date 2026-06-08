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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response, "登录成功");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ApiResponse.ok(response, "注册成功");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        LoginResponse response = authService.getCurrentUser(principal.getId());
        return ApiResponse.ok(response);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        authService.changePassword(principal.getId(), dto);
        return ApiResponse.ok(null, "密码修改成功");
    }
}
