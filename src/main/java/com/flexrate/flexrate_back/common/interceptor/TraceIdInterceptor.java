package com.flexrate.flexrate_back.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class TraceIdInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String username = "anonymous";
        if (request.getUserPrincipal() != null) {
            username = request.getUserPrincipal().getName();
        }
        MDC.put("loginId", username);

        String traceId = username + "-" + UUID.randomUUID();
        MDC.put("traceId", traceId);

        if (handler instanceof HandlerMethod handlerMethod) {
            String pageId = handlerMethod.getMethod().getName();
            MDC.put("pageId", pageId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}
