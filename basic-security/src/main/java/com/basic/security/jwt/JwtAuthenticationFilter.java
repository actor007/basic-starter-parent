package com.basic.security.jwt;

import com.basic.security.core.constant.SecurityConstant;
import com.basic.security.core.context.SecurityContextHolder;
import com.basic.security.core.exception.SecurityException;
import com.basic.security.core.model.LoginUser;
import com.basic.security.core.properties.SecurityProperties;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * <p>
 * 从请求头中提取 JWT token，解析用户信息并设置到 Security 上下文中。
 *
 * @author actor
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            try {
                LoginUser loginUser = jwtUtil.getLoginUserFromToken(token);
                if (loginUser != null) {
                    Set<SimpleGrantedAuthority> authorities = buildAuthorities(loginUser);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, token, authorities);
                    SecurityContextHolder.setAuthentication(authentication);
                    log.debug("JWT authentication success for user: {}", loginUser.getUserId());
                }
            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired for request: {}", request.getRequestURI());
                throw new SecurityException.TokenExpiredException("Token已过期，请重新登录");
            } catch (Exception e) {
                log.error("JWT authentication failed: {}", e.getMessage());
                throw new SecurityException.UnauthorizedException("Token认证失败");
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(securityProperties.getJwt().getTokenHeader());
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(securityProperties.getJwt().getTokenPrefix())) {
            return bearerToken.substring(securityProperties.getJwt().getTokenPrefix().length());
        }
        return null;
    }

    /**
     * 构建权限列表
     */
    private Set<SimpleGrantedAuthority> buildAuthorities(LoginUser loginUser) {
        Set<String> roles = loginUser.getRoles();
        if (roles == null || roles.isEmpty()) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 白名单路径不过滤
        return securityProperties.getAnonymousUrls().stream()
                .anyMatch(pattern -> matchPath(pattern, path));
    }

    private boolean matchPath(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            return path.startsWith(prefix);
        }
        return pattern.equals(path);
    }

}
