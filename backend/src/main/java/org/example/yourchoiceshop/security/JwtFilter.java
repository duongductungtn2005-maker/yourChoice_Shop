package org.example.yourchoiceshop.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                if (jwtUtil.validateToken(token)) {
                    Claims claims = jwtUtil.parseToken(token);
                    String role = claims.get("role", String.class);
                    String username = claims.get("username", String.class);
                    String subject = claims.getSubject();

                    // Kiểm tra an toàn trước khi ép kiểu
                    if (subject != null && subject.matches("\\d+")) { 
                        Integer userId = Integer.valueOf(subject);
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(userId);

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        System.out.println("⚠️ Token không hợp lệ: Subject không phải là số ID - Subject: " + subject);
                    }
                }
            } catch (Exception e) {
                // Đặt bẫy ở đây để log lỗi ra console thay vì bị Spring nuốt mất
                System.err.println("❌ Lỗi xử lý JWT Token: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
