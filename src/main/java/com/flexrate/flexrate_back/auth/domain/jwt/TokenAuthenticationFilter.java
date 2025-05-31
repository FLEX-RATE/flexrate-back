package com.flexrate.flexrate_back.auth.domain.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException{

        // 요청 헤더의 authorization 키의 값에서 접두사 제거
        String token = getAccessToken(request);

        // 가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정
        if (token != null && tokenProvider.validToken(token)) {
            Authentication auth = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 먼저 시도 (기존 방식 유지)
        String header = request.getHeader(HEADER_AUTHORIZATION);
        System.out.println("Authorization 헤더: " + header);

        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            String token = header.substring(TOKEN_PREFIX.length());
            System.out.println("헤더에서 추출한 토큰: " + token);
            return token;
        }

        // 2. 헤더가 없으면 쿼리 파라미터에서 시도 (SSE용)
        String token = request.getParameter("token");
        System.out.println("쿼리 파라미터 토큰: " + token);

        if (token != null && !token.trim().isEmpty()) {
            return token;
        }

        return null;
    }
}