package com.flexrate.flexrate_back.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 에러 코드 및 메시지
 * @since 2024.04.25
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 사용자 에러
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다"),

    // 대출
    INVALID_APPLICATION("L001", "신청 정보가 올바르지 않습니다."),
    LOAN_NOT_FOUND("L002", "대출 정보를 찾을 수 없습니다."),
    APPROVAL_MISSING("L003", "승인 정보가 누락되었습니다."),
    LOAN_ALREADY_APPROVED("L004", "이미 승인된 대출입니다."),

    // 인증/인가
    AUTH_REQUIRED_FIELD_MISSING("A000", "필수 입력값이 누락되었습니다."),
    EMAIL_ALREADY_REGISTERED("A001", "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS("A002", "아이디 또는 비밀번호가 일치하지 않습니다."),
    PASSKEY_AUTH_FAILED("A003", "패스키 인증에 실패했습니다."),
    INVALID_REFRESH_TOKEN("A004", "유효하지 않은 리프레시 토큰입니다."),

    // 파라미터
    PRODUCT_LOAD_ERROR("P001", "상품을 불러오는 중 오류가 발생했습니다."),
    PRODUCT_NOT_FOUND("P002", "해당 상품을 찾을 수 없습니다."),

    // 서버 오류
    INTERNAL_SERVER_ERROR("S500", "서버 내부 오류"),

    // 정적 리소스 404
    NOT_FOUND_STATIC_RESOURCE("S404", "요청한 정적 리소스를 찾을 수 없습니다."),

    // 유저/기타
    AUTHENTICATION_REQUIRED("M001", "인증이 필요합니다."),
    INVALID_EMAIL_FORMAT("M002", "올바르지 않은 이메일 형식입니다.");

    private final String code;
    private final String message;
}
