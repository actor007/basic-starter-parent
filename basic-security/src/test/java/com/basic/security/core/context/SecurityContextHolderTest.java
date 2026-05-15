package com.basic.security.core.context;

import com.basic.security.core.model.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityContextHolder 单元测试
 *
 * @author actor
 */
@DisplayName("SecurityContextHolder 上下文工具测试")
class SecurityContextHolderTest {

    private LoginUser testUser;

    @BeforeEach
    void setUp() {
        testUser = LoginUser.builder()
                .userId("user-001")
                .username("actor")
                .tenantId("tenant-01")
                .userType("admin")
                .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                .permissions(Set.of("user:read", "user:write"))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    // ========== get/set Authentication ==========

    @Test
    @DisplayName("设置 Authentication 后应能获取")
    void shouldGetAuthenticationAfterSet() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        Authentication result = SecurityContextHolder.getAuthentication();
        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("未设置 Authentication 时应返回 null")
    void shouldReturnNullWhenNotSet() {
        Authentication result = SecurityContextHolder.getAuthentication();
        assertThat(result).isNull();
    }

    // ========== getLoginUser ==========

    @Test
    @DisplayName("应正确获取 LoginUser")
    void shouldGetLoginUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        Optional<LoginUser> result = SecurityContextHolder.getLoginUser();
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("Principal 非 LoginUser 时应返回 empty")
    void shouldReturnEmptyWhenPrincipalIsNotLoginUser() {
        Authentication auth = new UsernamePasswordAuthenticationToken("plainUser", null);
        SecurityContextHolder.setAuthentication(auth);

        Optional<LoginUser> result = SecurityContextHolder.getLoginUser();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("无 Authentication 时应返回 empty")
    void shouldReturnEmptyWhenNoAuth() {
        Optional<LoginUser> result = SecurityContextHolder.getLoginUser();
        assertThat(result).isEmpty();
    }

    // ========== getUserId ==========

    @Test
    @DisplayName("应正确获取 userId")
    void shouldGetUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        assertThat(SecurityContextHolder.getUserId()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("无用户时应返回 null userId")
    void shouldReturnNullUserIdWhenNoUser() {
        assertThat(SecurityContextHolder.getUserId()).isNull();
    }

    // ========== getUsername ==========

    @Test
    @DisplayName("应正确获取 username")
    void shouldGetUsername() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        assertThat(SecurityContextHolder.getUsername()).isEqualTo("actor");
    }

    @Test
    @DisplayName("无用户时应返回 null username")
    void shouldReturnNullUsernameWhenNoUser() {
        assertThat(SecurityContextHolder.getUsername()).isNull();
    }

    // ========== getTenantId ==========

    @Test
    @DisplayName("应正确获取 tenantId")
    void shouldGetTenantId() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        assertThat(SecurityContextHolder.getTenantId()).isEqualTo("tenant-01");
    }

    @Test
    @DisplayName("无用户时应返回 null tenantId")
    void shouldReturnNullTenantIdWhenNoUser() {
        assertThat(SecurityContextHolder.getTenantId()).isNull();
    }

    // ========== getRoles ==========

    @Test
    @DisplayName("应正确获取角色集合")
    void shouldGetRoles() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        Set<String> roles = SecurityContextHolder.getRoles();
        assertThat(roles).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @DisplayName("无用户时应返回空集合")
    void shouldReturnEmptyRolesWhenNoUser() {
        Set<String> roles = SecurityContextHolder.getRoles();
        assertThat(roles).isEmpty();
    }

    // ========== getPermissions ==========

    @Test
    @DisplayName("应正确获取权限集合")
    void shouldGetPermissions() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        Set<String> permissions = SecurityContextHolder.getPermissions();
        assertThat(permissions).containsExactlyInAnyOrder("user:read", "user:write");
    }

    @Test
    @DisplayName("无用户时应返回空权限集合")
    void shouldReturnEmptyPermissionsWhenNoUser() {
        Set<String> permissions = SecurityContextHolder.getPermissions();
        assertThat(permissions).isEmpty();
    }

    // ========== clear ==========

    @Test
    @DisplayName("clear 后所有信息应被清空")
    void shouldClearContext() {
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null, Collections.emptyList());
        SecurityContextHolder.setAuthentication(auth);

        SecurityContextHolder.clear();

        assertThat(SecurityContextHolder.getAuthentication()).isNull();
        assertThat(SecurityContextHolder.getLoginUser()).isEmpty();
        assertThat(SecurityContextHolder.getUserId()).isNull();
    }
}
