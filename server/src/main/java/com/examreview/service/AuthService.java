package com.examreview.service;

import com.examreview.dto.ChangePasswordDTO;
import com.examreview.dto.LoginRequest;
import com.examreview.dto.LoginResponse;
import com.examreview.dto.RegisterRequest;

/**
 * 认证服务接口
 * 提供用户登录、注册、获取用户信息、修改密码等认证相关业务操作
 */
public interface AuthService {

    /**
     * 用户登录
     * 使用 Spring Security AuthenticationManager 进行用户名密码认证，成功后生成 JWT Token
     *
     * @param request 登录请求，包含用户名和密码
     * @return 登录响应，包含 JWT Token 和用户基本信息
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     * 校验用户名唯一性，使用 BCrypt 加密密码后保存用户，注册成功后生成 JWT Token
     *
     * @param request 注册请求，包含用户名、密码、昵称（可选）
     * @return 注册响应，包含 JWT Token 和用户基本信息
     */
    LoginResponse register(RegisterRequest request);

    /**
     * 获取当前登录用户信息
     * 根据用户ID查询用户详情并返回
     *
     * @param userId 用户ID
     * @return 用户信息响应
     */
    LoginResponse getCurrentUser(Long userId);

    /**
     * 修改密码
     * 验证原密码正确性后，使用 BCrypt 加密新密码并更新数据库
     *
     * @param userId 用户ID
     * @param dto    密码修改请求，包含原密码和新密码
     */
    void changePassword(Long userId, ChangePasswordDTO dto);
}
