package com.flexrate.flexrate_back.common.util;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.AdminAuthChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Principal;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class AdminActionTemplate {
    private final AdminAuthChecker adminAuthChecker;

    /**
     * 관리자 인증, 요청/응답 로깅 공통 처리 래퍼
     */
    public <T> T execute(String actionName, Principal principal, Supplier<T> action) {
        String principalName = principal != null ? principal.getName() : "anonymous";
        log.info("{} 요청 by principal={}", actionName, principalName);

        if (!adminAuthChecker.isAdmin(principal)) {
            log.warn("관리자 인증 실패 {} principal={}", actionName, principalName);
            throw new FlexrateException(ErrorCode.ADMIN_AUTH_REQUIRED);
        }

        T result = action.get();
        log.info("{} 성공 by principal={}", actionName, principalName);
        return result;
    }
}
