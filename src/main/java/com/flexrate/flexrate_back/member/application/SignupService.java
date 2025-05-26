package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.application.RefreshTokenService;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.util.StringRedisUtil;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.AnalyzeConsumptionTypeResponse;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupPasswordRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final DummyFinancialDataGenerator dummyFinancialDataGenerator;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisUtil stringRedisUtil;

    // 비밀번호 기반 회원가입
    public SignupResponseDTO registerByPassword(SignupPasswordRequestDTO dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String hashedPwd = passwordEncoder.encode(dto.password());

        Member member = Member.builder()
                .age(Period.between(dto.birthDate(), LocalDate.now()).getYears())
                .email(dto.email())
                .passwordHash(hashedPwd)
                .name(dto.name())
                .sex(dto.sex())
                .birthDate(dto.birthDate())
                .consumptionType(dto.consumptionType())
                .consumeGoal(dto.consumeGoal())
                .status(MemberStatus.ACTIVE)
                .role(Role.MEMBER)
                .build();

        Member saved = memberRepository.save(member);
        dummyFinancialDataGenerator.generateDummyFinancialData(saved);

        // ✅ 로그인 시와 동일한 방식으로 토큰 발급 및 Redis 저장
        String accessToken = jwtTokenProvider.generateToken(saved, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(saved, Duration.ofDays(7));
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(saved.getMemberId()), Duration.ofDays(7));

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public void addFidoCredential(Long memberId, PasskeyRequestDTO dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        webAuthnService.registerPasskey(member, dto);
    }

    public String generateFidoChallenge(Long memberId) {
        return webAuthnService.generateChallenge(memberId);
    }

    // 임시 소비성향 도출 메서드
    public AnalyzeConsumptionTypeResponse analyzeConsumptionType() {
        ConsumptionType[] types = ConsumptionType.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(types.length);
        ConsumptionType randomType = types[randomIndex];

        return AnalyzeConsumptionTypeResponse.builder()
                .consumptionType(randomType)
                .build();
    }

}