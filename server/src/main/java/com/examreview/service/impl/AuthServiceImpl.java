package com.examreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.examreview.dto.ChangePasswordDTO;
import com.examreview.dto.LoginRequest;
import com.examreview.dto.LoginResponse;
import com.examreview.dto.RegisterRequest;
import com.examreview.entity.User;
import com.examreview.exception.BusinessException;
import com.examreview.mapper.UserMapper;
import com.examreview.security.UserPrincipal;
import com.examreview.service.AuthService;
import com.examreview.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 使用 Spring Security 的 AuthenticationManager 进行认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(principal.getId(), principal.getUsername());

        User user = userMapper.selectById(principal.getId());
        return new LoginResponse(
                token, principal.getId(), principal.getUsername(),
                user != null ? user.getNickname() : principal.getUsername(),
                user != null && user.getRole() != null ? user.getRole() : "user"
        );
    }

    @Override
    public LoginResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        userMapper.insert(user);

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        return new LoginResponse(
                token, user.getId(), user.getUsername(), user.getNickname(),
                user.getRole() != null ? user.getRole() : "user"
        );
    }

    @Override
    public LoginResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return new LoginResponse(null, user.getId(), user.getUsername(), user.getNickname(),
                user.getRole() != null ? user.getRole() : "user");
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    private String getUserNickname(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getNickname() : "";
    }
}
