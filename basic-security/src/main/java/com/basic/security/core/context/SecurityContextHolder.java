package com.basic.security.core.context;

import com.basic.security.core.model.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * 自定义 Security 上下文工具
 * <p>
 * 方便获取当前登录用户信息。
 *
 * @author actor
 */
public final class SecurityContextHolder {

    private SecurityContextHolder() {
    }

    /**
     * 获取当前认证对象
     */
    public static Authentication getAuthentication() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 设置当前认证对象
     */
    public static void setAuthentication(Authentication authentication) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 获取当前登录用户（LoginUser）
     */
    public static Optional<LoginUser> getLoginUser() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return Optional.of((LoginUser) auth.getPrincipal());
        }
        return Optional.empty();
    }

    /**
     * 获取当前用户ID
     */
    public static String getUserId() {
        return getLoginUser().map(LoginUser::getUserId).orElse(null);
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        return getLoginUser().map(LoginUser::getUsername).orElse(null);
    }

    /**
     * 获取当前租户ID
     */
    public static String getTenantId() {
        return getLoginUser().map(LoginUser::getTenantId).orElse(null);
    }

    /**
     * 获取当前用户角色
     */
    public static Set<String> getRoles() {
        return getLoginUser().map(LoginUser::getRoles).orElse(Collections.emptySet());
    }

    /**
     * 获取当前用户权限
     */
    public static Set<String> getPermissions() {
        return getLoginUser().map(LoginUser::getPermissions).orElse(Collections.emptySet());
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

}
