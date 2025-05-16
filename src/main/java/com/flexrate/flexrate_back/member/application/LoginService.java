package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.application.TokenService;
import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.util.StringRedisUtil;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MfaLogRepository;
import com.flexrate.flexrate_back.member.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final MfaLogRepository mfaLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisUtil stringRedisUtil;

    public LoginResponseDTO loginWithPassword(PasswordLoginRequestDTO request) {

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        authenticateWithPassword(request, member);

        // JwtTokenProvider의 generateToken 호출
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));  // 2시간 만료
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));  // 7일 만료

        // Redis에 refreshToken 저장 (7일 만료)
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofDays(7));

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .challenge("")
                .build();
    }

    public LoginResponseDTO loginWithPasskey(PasskeyLoginRequestDTO request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 인증 및 Challenge 유효성 검증
        FidoCredential credential = webAuthnService.authenticatePasskey(
                member.getMemberId(),
                request.passkeyData(),
                request.challenge()
        ).orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));

        // JWT 발급
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));  // 2시간 만료
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));  // 7일 만료

        // Redis 저장
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofDays(7));

        // MFA 로그 저장
        MfaLog mfaLog = MfaLog.builder()
                .mfaType(MfaType.FIDO2)
                .authenticatedAt(LocalDateTime.now())
                .result(AuthResult.SUCCESS)
                .deviceInfo(request.deviceInfo())
                .build();
        mfaLogRepository.save(mfaLog);

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .challenge("") // 인증 완료 상태
                .build();
    }

    public LoginResponseDTO loginWithMFA(MfaLoginRequestDTO request) {
        // 사용자 존재 여부 확인
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // WebAuthn 인증
        PasskeyAuthenticationDTO passkeyAuthDTO = PasskeyAuthenticationDTO.builder()
                .clientDataJSON(request.clientDataJSON())
                .authenticatorData(request.authenticatorData())
                .signature(request.signature())
                .build();

        FidoCredential credential = webAuthnService
                .authenticatePasskey(member.getMemberId(), passkeyAuthDTO, request.challenge())
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));

        // 인증 성공 → 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));

        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofDays(7));

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .challenge("") // 추후 필요 시 재사용
                .build();
    }


    private void authenticateWithPassword(PasswordLoginRequestDTO request, Member member) {

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

    }

    public void logout(String refreshToken) {
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.delete(redisKey);
    }
}