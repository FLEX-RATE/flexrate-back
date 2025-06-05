package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final MfaLogRepository mfaLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisUtil stringRedisUtil;

    public LoginResponseDTO loginWithPassword(PasswordLoginRequestDTO request) {
        log.info("로그인 시도: email={}", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자 email={}", request.email());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        authenticateWithPassword(request, member);

        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));  // 2시간 만료
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));  // 7일 만료
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofSeconds(60));

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .username(member.getName())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(member.getRole())
                .challenge("")
                .build();
    }

    public LoginResponseDTO loginWithPasskey(PasskeyLoginRequestDTO request) {
        log.info("Passkey 로그인 시도 email={}", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Passkey 로그인: 존재하지 않는 사용자 email={}", request.email());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        // Passkey 인증 및 Challenge 검증
        FidoCredential credential = webAuthnService.authenticatePasskey(
                member.getMemberId(),
                request.passkeyData(),
                request.challenge()
        ).orElseThrow(() -> {
            log.warn("Passkey 인증 실패 userId={}, email={}", member.getMemberId(), member.getEmail());
            return new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        });

        // JWT 발급
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));  // 2시간 만료
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));  // 7일 만료
        log.info("JWT 토큰 발급 완료 userId={}", member.getMemberId());

        // Redis 저장
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofDays(7));
        log.info("RefreshToken Redis 저장 완료 userId={}", member.getMemberId());

        // MFA 로그 저장
        MfaLog mfaLog = MfaLog.builder()
                .mfaType(MfaType.FIDO2)
                .authenticatedAt(LocalDateTime.now())
                .result(AuthResult.SUCCESS)
                .deviceInfo(request.deviceInfo())
                .build();
        mfaLogRepository.save(mfaLog);
        log.info("MFA 인증 로그 저장 userId={}", member.getMemberId());

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .username(member.getName())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(member.getRole())
                .challenge("")
                .build();
    }

    public LoginResponseDTO loginWithMFA(MfaLoginRequestDTO request) {
        log.info("MFA 로그인 시도 memberId={}", request.memberId());

        // 사용자 존재 여부 확인
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> {
                    log.warn("MFA 로그인: 존재하지 않는 사용자 memberId={}", request.memberId());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });
        // WebAuthn 인증
        PasskeyAuthenticationDTO passkeyAuthDTO = PasskeyAuthenticationDTO.builder()
                .clientDataJSON(request.clientDataJSON())
                .authenticatorData(request.authenticatorData())
                .signature(request.signature())
                .build();

        FidoCredential credential = webAuthnService
                .authenticatePasskey(member.getMemberId(), passkeyAuthDTO, request.challenge())
                .orElseThrow(() -> {
                    log.warn("MFA 인증 실패 userId={}, email={}", member.getMemberId(), member.getEmail());
                    return new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
                });

        // 인증 성공 → 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));
        log.info("JWT 토큰 발급 완료 userId={}", member.getMemberId());

        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofDays(7));
        log.info("RefreshToken Redis 저장 완료 userId={}", member.getMemberId());

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .username(member.getName())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(member.getRole())
                .challenge("")
                .build();
    }


    private void authenticateWithPassword(PasswordLoginRequestDTO request, Member member) {
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            log.warn("비밀번호 불일치 userId={}", member.getMemberId());
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    public void logout(String refreshToken) {
        String redisKey = "refreshToken:" + refreshToken;
        stringRedisUtil.delete(redisKey);
        log.info("RefreshToken 삭제 완료 redisKey={}", redisKey.substring(0, Math.min(20, redisKey.length())) + "...");
    }
}