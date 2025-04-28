package com.flexrate.flexrate_back.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 사용자가 잘못된 데이터를 입력할 경우 예외처리
 * @return 회원 목록
 * @since 2025.04.28
 * @author 윤영찬
 */


@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}