package com.flexrate.flexrate_back.auth.domain.jwt;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\": \"%s\", \"message\": \"%s\"}",
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getMessage()));

        try {
            MDC.put("loginId", "anonymous");
            MDC.put("errorCode", ErrorCode.UNAUTHORIZED.getCode());
            MDC.put("traceId", request.getHeader("X-Trace-Id"));
            log.warn("{}", ErrorCode.UNAUTHORIZED.getMessage());
        } finally {
            MDC.clear();
        }
    }
}
