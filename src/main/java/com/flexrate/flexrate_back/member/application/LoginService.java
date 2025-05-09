package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MfaLogRepository;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final MfaLogRepository mfaLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final RedisTemplate<String, String> redisTemplate;

    // 로그인 방식에 따른 인증 처리
    public LoginResponseDTO login(LoginRequestDTO request) {
        String challenge = switch (request.loginMethod()) {
            case PASSWORD -> authenticateWithPassword(request);
            case PASSKEY -> authenticateWithPasskey(request);
            default -> throw new FlexrateException(ErrorCode.AUTHENTICATION_REQUIRED);
        };

        // 인증 방식에 따른 추가 인증 처리
        switch (request.authMethod()) {
            case MFA:
                authenticateWithMfa(request);
                break;
            case FIDO:
                challenge = authenticateWithFido(request); // FIDO 인증
                break;
            default:
                throw new FlexrateException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        // 토큰 생성 및 반환
        return generateTokens(request.email(), challenge);
    }

    private String authenticateWithPassword(LoginRequestDTO request) {
        // 이메일을 통해 Member 객체 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }
        return null; // 추가 인증 필요 시 challenge 반환
    }

    private String authenticateWithPasskey(LoginRequestDTO request) {
        // 이메일을 통해 Member 객체 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // Passkey 인증 후 challenge 생성
        return webAuthnService.generateChallenge(member.getMemberId());
    }

    private void authenticateWithMfa(LoginRequestDTO request) {
        // 이메일을 통해 Member 객체 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // MFA 인증 처리
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        // MFA 인증 로그 생성
        MfaType mfaType = request.mfaType();
        createMfaLog(request, member, mfaType);
    }

    private String authenticateWithFido(LoginRequestDTO request) {
        // 이메일을 통해 Member 객체 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // FIDO 인증 처리
        if (request.passkeyData() == null || request.passkeyData().isEmpty()) {
            // Challenge 발급
            return webAuthnService.generateChallenge(member.getMemberId());
        }

        if (request.challenge() == null || !request.challenge().equals(request.challenge())) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        // Challenge 검증 및 인증
        boolean isValid = webAuthnService.authenticatePasskey(member.getMemberId(), request.passkeyData(), request.challenge()).isPresent();
        if (!isValid) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        return request.challenge(); // challenge 반환
    }

    private LoginResponseDTO generateTokens(String email, String challenge) {
        // 이메일을 통해 Member 객체 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(14));

        // refreshToken Redis에 저장
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("refreshToken:" + member.getMemberId(), refreshToken, Duration.ofDays(14));

        // 로그인 응답 반환
        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .challenge(challenge)
                .build();
    }

    private void createMfaLog(LoginRequestDTO request, Member member, MfaType mfaType) {
        // MFA 로그 생성
        MfaLog mfaLog = MfaLog.builder()
                .mfaType(mfaType)
                .result(AuthResult.SUCCESS)
                .authenticatedAt(LocalDateTime.now())
                .deviceInfo(request.deviceInfo())
                .transaction(null)
                .build();

        mfaLogRepository.save(mfaLog);
    }

    public void logout(Long memberId) {
        redisTemplate.delete("refreshToken:" + memberId); // Redis에서 refreshToken 삭제
    }

    public String refreshAccessToken(Long memberId) {
        String refreshToken = redisTemplate.opsForValue().get("refreshToken:" + memberId);

        if (refreshToken == null) {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        return jwtTokenProvider.generateToken(member, Duration.ofHours(2));
    }

    public void blacklistRefreshToken(Long memberId) {
        String refreshToken = redisTemplate.opsForValue().get("refreshToken:" + memberId);

        if (refreshToken != null) {
            redisTemplate.opsForValue().set("blacklist:refreshToken:" + memberId, refreshToken, Duration.ofDays(14));
            redisTemplate.delete("refreshToken:" + memberId);
        } else {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
