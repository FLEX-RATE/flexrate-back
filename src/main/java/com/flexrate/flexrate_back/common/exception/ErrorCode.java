package com.flexrate.flexrate_back.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 에러 코드 및 메시지
 * @since 2024.04.25
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    REDIS_NOT_FOUND("C001", "요청한 데이터가 존재하지 않거나 만료되었습니다.", HttpStatus.BAD_REQUEST),

    // 사용자 에러
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATED_EMAIL("U002", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),


    // 대출
    INVALID_APPLICATION("L001", "신청 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    LOAN_NOT_FOUND("L002", "대출 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    APPROVAL_MISSING("L003", "승인 정보가 누락되었습니다.", HttpStatus.BAD_REQUEST),
    LOAN_ALREADY_APPROVED("L004", "이미 승인된 대출입니다.", HttpStatus.CONFLICT),
    LOAN_STATUS_CONFLICT("L005", "변경을 요청한 상태가 제약 조건에 위배됩니다.", HttpStatus.BAD_REQUEST),
    LOAN_SERVER_ERROR("L006", "대출 서버 응답이 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LOAN_APPLICATION_ALREADY_EXISTS("L007", "이미 대출 중에 있습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LOAN_PRODUCT_NOT_FOUND("L008", "대출 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    LOAN_REQUEST_CONFLICT("L009", "대출 신청 요청이 제약 조건에 위배됩니다.", HttpStatus.BAD_REQUEST),
    LOAN_NOT_APPLIED("L010", "신청을 하지 않은 대출입니다.", HttpStatus.BAD_REQUEST),
    LOAN_CONSUMPTION_TYPE_MISMATCH("L011", "소비 유형이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 리포트
    REPORT_MEMBER_OR_MONTH_NULL("R001", "member 또는 month는 null일 수 없습니다.", HttpStatus.BAD_REQUEST),
    REPORT_ALREADY_EXISTS("R002", "해당 월의 소비습관 리포트가 이미 존재합니다.", HttpStatus.CONFLICT),
    NO_CONSUMPTION_DATA("R003", "해당 월의 소비데이터가 존재하지 않습니다.", HttpStatus.CONFLICT),
    REPORT_DOESNT_EXISTS("R004", "해당 월의 소비습관 리포트가 존재하지 않습니다.", HttpStatus.CONFLICT),
    JSON_SERIALIZATION_ERROR("R005", "소비 데이터 직렬화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    JSON_DESERIALIZATION_ERROR("R006", "소비 데이터 역직렬화에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // GPT
    OPENAI_ERROR("GPT001", "OpenAI API 통신 오류", HttpStatus.CONFLICT),

    // 금리
    NO_INTEREST("I001", "저장된 금리가 없습니다.", HttpStatus.NOT_FOUND),

    // 이메일 인증
    MESSAGING_EXCEPTION("M001", "이메일 오류가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ALREADY_USING("M002", "이메일 오류가 발생하였습니다.", HttpStatus.BAD_REQUEST),
    WRONG_AUTH_CODE("M003", "제공된 인증코드가 불일치합니다.", HttpStatus.BAD_REQUEST),

    // 인증/인가
    AUTH_REQUIRED_FIELD_MISSING("A000", "필수 입력값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_REGISTERED("A001", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_CREDENTIALS("A002", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    PASSKEY_AUTH_FAILED("A003", "패스키 인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("A004", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_EMAIL_FORMAT("A005", "올바르지 않은 이메일 형식입니다.", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_REQUIRED("A006", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ADMIN_AUTH_REQUIRED("A007", "관리자 권한이 필요합니다.", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_NOT_FOUND("A008", "리프레쉬 토큰이 누락되었습니다.", HttpStatus.BAD_REQUEST),

    // 상품
    PRODUCT_LOAD_ERROR("P001", "상품을 불러오는 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND("P002", "해당 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 서버 오류
    INTERNAL_SERVER_ERROR("S500", "서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND_STATIC_RESOURCE("S404", "요청한 정적 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 유효성
    VALIDATION_ERROR("V001", "유효성 검사 오류", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
