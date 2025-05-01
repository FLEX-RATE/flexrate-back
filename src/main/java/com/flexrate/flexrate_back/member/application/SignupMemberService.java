package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupMemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SignupMemberService(MemberRepository memberRepository,
                               PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;

    }

    /*
     * 회원 가입 처리
     * @param signupDTO 회원가입 요청 데이터
     * @return 저장된 회원 객체
     * @throws FlexrateException 이메일 누락, 형식 오류, 중복 또는 비밀번호 유효성 검사 실패 시 예외 발생
     * @since 2025.04.29
     * @author 윤영찬
     */
    public Member registerMember(SignupDTO signupDTO) {
        if (signupDTO.getEmail() == null || signupDTO.getEmail().isEmpty()) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }
        if (!signupDTO.getEmail().contains("@")) {
            throw new FlexrateException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (memberRepository.existsByEmail(signupDTO.getEmail())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String rawPwd = signupDTO.getPassword();
        if (rawPwd == null || rawPwd.length() < 6
                || !rawPwd.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,}$")) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }
        String hashedPwd = passwordEncoder.encode(rawPwd);

        Member member = Member.builder()
                .email(signupDTO.getEmail())
                .passwordHash(hashedPwd)
                .name(signupDTO.getName())
                .sex(Sex.valueOf(signupDTO.getSex().toUpperCase()))
                .birthDate(signupDTO.getBirthDate())
                .status(MemberStatus.ACTIVE)
                .build();

        return memberRepository.save(member);
    }




}
