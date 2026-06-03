package com.examreview.util;

import com.examreview.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类：从 Spring Security 上下文中获取当前登录用户信息
 */
public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
            if (userId == null) {
                throw new RuntimeException("当前用户ID为空，请重新登录");
            }
            return userId;
        }
        throw new RuntimeException("未获取到用户认证信息，请重新登录");
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    /**
     * 当前用户是否为管理员
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).isAdmin();
        }
        return false;
    }
}
