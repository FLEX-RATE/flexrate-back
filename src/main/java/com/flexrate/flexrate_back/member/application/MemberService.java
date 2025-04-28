package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.exception.ErrorCode;

public class MemberService {

    // 회원 가입 유효성 검사
    public void signup(SignupDTO signupDTO) {
        if (signupDTO.getEmail() == null || signupDTO.getEmail().isEmpty()) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        if (!isValidEmail(signupDTO.getEmail())) {
            throw new FlexrateException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        if (isEmailAlreadyRegistered(signupDTO.getEmail())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        if (!isValidPassword(signupDTO.getPassword())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS); // 비밀번호 유효성 검사 실패 시
        }
    }

    // 이메일 유효성 검사
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    private boolean isEmailAlreadyRegistered(String email) {
        // DB에서 이메일 중복 확인 로직을 추가해야 합니다.
        return false; // 예시로 false 리턴
    }


    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,}$");
    }
}
