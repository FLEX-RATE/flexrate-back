package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FidoCredentialRepository fidoCredentialRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /*
     * 회원가입 중복 이메일을 체크하고, 회원을 등록한 후, 생성된 회원 정보를 응답
     * @since 2025.05.03
     * @author 윤영찬
     * */

    public SignupResponseDTO registerMember(SignupRequestDTO signupDTO) {
        if (memberRepository.existsByEmail(signupDTO.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String rawPwd = signupDTO.password();
        String hashedPwd = passwordEncoder.encode(rawPwd);

        ConsumptionType consumptionType;
        ConsumeGoal consumeGoal;
        Sex sex = Sex.valueOf(signupDTO.sex());

        try {
            consumptionType = signupDTO.consumptionType();
            consumeGoal = signupDTO.consumeGoal();
        } catch (IllegalArgumentException e) {
            throw new FlexrateException(ErrorCode.VALIDATION_ERROR);
        }

        Member member = Member.builder()
                .email(signupDTO.email())
                .passwordHash(hashedPwd)
                .name(signupDTO.name())
                .sex(sex)
                .birthDate(signupDTO.birthDate())
                .status(MemberStatus.ACTIVE)
                .consumptionType(consumptionType)
                .consumeGoal(consumeGoal)
                .role(Role.MEMBER)
                .build();


        Member saved = memberRepository.save(member);

        if (signupDTO.passkeys() != null && !signupDTO.passkeys().isEmpty()) {
            savePasskeys(saved, signupDTO.passkeys()); // saved로 수정
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(saved, Duration.ofHours(1));

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .accessToken(accessToken)
                .build();
    }

    private void savePasskeys(Member member, List<PasskeyRequestDTO> passkeys) {
        for (PasskeyRequestDTO passkey : passkeys) {
            FidoCredential fidoCredential = FidoCredential.builder()
                    .member(member) // 해당 회원과 연관
                    .publicKey(passkey.publicKey()) // 패스키의 공개키
                    .signCount(passkey.signCount()) // 서명 카운트
                    .deviceInfo(passkey.deviceInfo()) // 디바이스 정보
                    .isActive(passkey.isActive()) // 활성화 여부
                    .lastUsedDate(LocalDateTime.now()) // 서버에서 자동 기록
                    .build();

            fidoCredentialRepository.save(fidoCredential); // FidoCredential DB에 저장
        }
    }

    // 회원 ID로 회원 조회
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));
    }
}
