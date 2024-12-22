package com.gijun.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getRequestURI();
//        boolean shouldNotFilter = path.startsWith("/api/public/") ||
//                path.startsWith("/api/swagger-ui/") ||
//                path.startsWith("/api/v3/api-docs/") ||
//                path.equals("/api/swagger-ui.html") ||
//                path.equals("/api/v3/api-docs") ||
//                path.startsWith("/api/auth/");
//
//        log.info("Path: {}, Should skip filter: {}", path, shouldNotFilter);
//        return shouldNotFilter;
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            log.error("valid Check : " + jwtTokenProvider.validateToken(token));
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication auth = jwtTokenProvider.getAuthentication(token);
                if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                    log.info("CustomUserDetails Info: {}", userDetails);
                }
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Token validated and authentication set for path: {}", request.getRequestURI());
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}