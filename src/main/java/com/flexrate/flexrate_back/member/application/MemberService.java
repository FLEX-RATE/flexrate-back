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

import java.time.LocalDateTime;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MemberService(MemberRepository memberRepository,
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



    /*
     * 로그인 인증 처리
     *
     * @param email 로그인 시 입력한 이메일
     * @param password 로그인 시 입력한 비밀번호
     * @return 인증 성공 여부 (true)
     * @throws FlexrateException 이메일이 존재하지 않거나 비밀번호가 일치하지 않으면 예외 발생
     * @since 2025.04.29
     * @author 윤영찬
     */
//    public boolean authenticate(String email, String password) {
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));
//
//        // 비밀번호 확인
//        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
//            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
//        }
//
//        // 로그인 성공 시 Member 객체를 수정 (toBuilder 사용)
//        Member updatedMember = member.toBuilder()
//                .lastLoginAt(LocalDateTime.now())
//                .lastLoginMethod(LoginMethod.PASSWORD)
//                .build();
//
//        System.out.println("Updated lastLoginAt: " + updatedMember.getLastLoginAt());
//        memberRepository.save(updatedMember);
//
//        return true;
//    }


    /*
     * 비밀번호 변경
     *
     * @param dto 비밀번호 변경 요청 데이터
     * @throws FlexrateException 현재 비밀번호가 틀리거나 새 비밀번호가 유효하지 않을 경우 예외 발생
     * @since 2025.04.29
     * @author 윤영찬
     */
//    public void changePassword(PasswordChangeDTO dto) {
//        Member member = memberRepository.findByEmail(dto.getEmail())
//                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));
//
//        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPasswordHash())) {
//            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
//        }
//
//        String newPassword = dto.getNewPassword();
//        if (newPassword.length() < 6 ||
//                !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).{6,}$")) {
//            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
//        }
//
//        String newHashed = passwordEncoder.encode(newPassword);
//
//        Member updated = member.toBuilder()
//                .passwordHash(newHashed)
//                .passwordLastChangedAt(LocalDateTime.now()) // 갱신
//                .build();
//
//        memberRepository.save(updated);
//    }
}
