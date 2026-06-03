package com.examreview.exception;

import com.examreview.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .collect(Collectors.joining("；"));
        return ApiResponse.fail(400, msg);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponse<Void> handleBadCredentials(BadCredentialsException e) {
        log.warn("登录失败: 用户名或密码错误");
        return ApiResponse.fail(401, "用户名或密码错误");
    }

    @ExceptionHandler({UsernameNotFoundException.class, DisabledException.class, LockedException.class})
    public ApiResponse<Void> handleAuthenticationException(Exception e) {
        log.warn("认证失败: {}", e.getMessage());
        return ApiResponse.fail(401, "用户名或密码错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        String msg = "请求数据格式错误";
        // 提取 Jackson 解析异常的根因消息，帮助调用方排查
        Throwable cause = e.getRootCause();
        if (cause != null && cause.getMessage() != null) {
            String causeMsg = cause.getMessage();
            if (causeMsg.contains("Invalid UTF-8")) {
                msg = "请求数据包含非法字符，请使用 UTF-8 编码";
            }
        }
        return ApiResponse.fail(400, msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return ApiResponse.fail(500, "服务器内部错误，请稍后重试");
    }
}
