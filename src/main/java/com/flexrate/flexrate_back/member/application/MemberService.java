package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 회원 가입 유효성 검사 및 처리
    public void signup(SignupDTO signupDTO) {
        // 이메일 유효성 검사
        if (signupDTO.getEmail() == null || signupDTO.getEmail().isEmpty()) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        if (!isValidEmail(signupDTO.getEmail())) {
            throw new FlexrateException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        // 이메일 중복 확인
        if (isEmailAlreadyRegistered(signupDTO.getEmail())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        // 비밀번호 유효성 검사
        if (!isValidPassword(signupDTO.getPassword())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS); // 비밀번호 유효성 검사 실패 시
        }

        // 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(signupDTO.getPassword());

        // Member 객체 생성 후 저장
        Member member = Member.builder()
                .email(signupDTO.getEmail())
                .passwordHash(hashedPassword)
                .name(signupDTO.getName())
                .sex(signupDTO.getSex())
                .status(MemberStatus.ACTIVE)  // 기본 상태로 ACTIVE 설정
                .build();

        memberRepository.save(member);
    }

    // 이메일 유효성 검사
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    // 이메일 중복 확인
    private boolean isEmailAlreadyRegistered(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 비밀번호 유효성 검사
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,}$");
