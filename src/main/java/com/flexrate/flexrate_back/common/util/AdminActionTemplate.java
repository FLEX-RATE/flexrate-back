package com.flexrate.flexrate_back.common.util;

import com.flexrate.flexrate_back.auth.resolver.CurrentMemberId;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.AdminAuthChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminActionTemplate {
    private final AdminAuthChecker adminAuthChecker;

    /**
     * 관리자 인증, 요청/응답 로깅 공통 처리 래퍼
     */
    public <T> T execute(String actionName, @CurrentMemberId Long memberId, Supplier<T> action) {
        log.info("{} 요청", actionName);

        if (!adminAuthChecker.isAdmin(memberId)) {
            log.warn("관리자 인증 실패 {}", actionName);
            throw new FlexrateException(ErrorCode.ADMIN_AUTH_REQUIRED);
        }

        T result = action.get();
        log.info("{} 성공", actionName);
        return result;
    }
}
