package com.examreview.service;

import com.examreview.dto.ChangePasswordDTO;
import com.examreview.dto.LoginRequest;
import com.examreview.dto.LoginResponse;
import com.examreview.dto.RegisterRequest;

public interface AuthService {

    /**
     * 用户登录，返回 JWT Token 和用户信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册，返回 JWT Token 和用户信息
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 获取当前登录用户信息
     */
    LoginResponse getCurrentUser(Long userId);
    void changePassword(Long userId, ChangePasswordDTO dto);
}
