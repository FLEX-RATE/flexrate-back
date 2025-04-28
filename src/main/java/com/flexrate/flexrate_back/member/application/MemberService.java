package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


/*
 * 회원 가입 처리 (이메일 및 비밀번호 유효성 검증 포함)
 * @param signupDTO 회원 가입 요청 데이터
 * @throws FlexrateException 필수 입력 누락, 이메일 중복, 입력 오류 등 발생 시
 * @return 없음 (성공 시 DB에 회원 저장)
 * @since 2025.04.28
 * @author 윤영찬
 */

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository, BCryptPasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }


        String hashedPassword = passwordEncoder.encode(signupDTO.getPassword());


        Member member = Member.builder()
                .email(signupDTO.getEmail())
                .passwordHash(hashedPassword)
                .name(signupDTO.getName())
                .sex(convertToSex(signupDTO.getSex()))
                .birthDate(signupDTO.getBirthDate())
                .status(MemberStatus.ACTIVE)
                .build();

        memberRepository.save(member);
    }


    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }


    private boolean isEmailAlreadyRegistered(String email) {
        return memberRepository.existsByEmail(email);
    }


    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        return password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,}$");
    }

    private Sex convertToSex(String sex) {
        try {
            return Sex.valueOf(sex);
        } catch (IllegalArgumentException e) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS); // 잘못된 sex 값 처리
        }
    }
}