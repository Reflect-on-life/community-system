package com.example.futurecommunity.config;

import com.example.futurecommunity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 跳过对登录路径和统计路径的JWT验证
        if (request.getRequestURI().startsWith("/api/auth/") || request.getRequestURI().startsWith("/api/statistics/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        if (token != null && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            List<String> roles = jwtUtil.getRolesFromToken(token);
            String type = jwtUtil.getTypeFromToken(token);

            logger.info("解析token获取到的信息 - userId: {}, roles: {}, type: {}", userId, roles, type);

            if (userId != null) {
                List<String> authorities = new ArrayList<>();
                
                // 如果roles字段存在且不为空，使用roles字段
                if (roles != null && !roles.isEmpty()) {
                    authorities.addAll(roles);
                    logger.info("使用token中的roles字段: {}", roles);
                } 
                // 否则，回退到使用type字段生成角色权限
                else if (type != null) {
                    String role = "ROLE_" + type.toUpperCase();
                    authorities.add(role);
                    logger.info("使用type字段生成角色: {}", role);
                } else {
                    logger.warn("token中既没有roles字段也没有type字段，用户将没有任何角色权限");
                }

                logger.info("最终用户拥有的权限: {}", authorities);

                // 即使authorities为空，也要创建一个UserDetails对象，以便后续的权限检查能够正常进行
                UserDetails userDetails = User.builder()
                        .username(userId.toString())
                        .password("")
                        .authorities(authorities.toArray(new String[0]))
                        .build();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            logger.info("未检测到有效的JWT token或token无效");
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
