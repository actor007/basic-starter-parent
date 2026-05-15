package com.basic.security.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 登录用户信息模型
 * <p>
 * 统一抽象不同认证模式（JWT/OAuth2/Session）下的用户信息。
 *
 * @author actor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户唯一标识
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户类型：admin / normal / open
     */
    private String userType;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 角色列表
     */
    private Set<String> roles;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 扩展属性（自定义）
     */
    private java.util.Map<String, Object> ext;

    /**
     * 获取权限列表（兼容 Spring Security 的 GrantedAuthority 风格）
     * <p>合并 roles 和 permissions。</p>
     */
    public Collection<String> getAuthorities() {
        return Stream.concat(
                roles != null ? roles.stream() : Stream.empty(),
                permissions != null ? permissions.stream() : Stream.empty()
        ).collect(Collectors.toList());
    }

}
