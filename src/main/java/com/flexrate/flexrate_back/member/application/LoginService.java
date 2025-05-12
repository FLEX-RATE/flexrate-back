package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.application.TokenService;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MfaLogRepository;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import com.flexrate.flexrate_back.member.dto.MfaLoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.PasskeyLoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.PasswordLoginRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

    private final MemberRepository memberRepository;
    private final MfaLogRepository mfaLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO loginWithPassword(PasswordLoginRequestDTO request) {
        log.info("이메일: {} 로 패스워드 로그인 시작", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("이메일: {} 에 해당하는 사용자가 존재하지 않습니다.", request.email());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        authenticateWithPassword(request, member);
        log.info("이메일: {} 의 패스워드 로그인 성공", request.email());

        return generateTokens(member, "");
    }

    public LoginResponseDTO loginWithPasskey(PasskeyLoginRequestDTO request) {
        log.info("이메일: {} 로 Passkey 로그인 시작", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("이메일: {} 에 해당하는 사용자가 존재하지 않습니다.", request.email());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        String challenge = authenticateWithPasskey(request, member);
        log.info("이메일: {} 의 Passkey 로그인 성공", request.email());

        return generateTokens(member, challenge);
    }

    public LoginResponseDTO loginWithMfa(MfaLoginRequestDTO request) {
        log.info("이메일: {} 로 MFA 로그인 시작", request.email());

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("이메일: {} 에 해당하는 사용자가 존재하지 않습니다.", request.email());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        authenticateWithMfa(request, member);
        log.info("이메일: {} 의 MFA 로그인 성공", request.email());

        return generateTokens(member, "");
    }

    private void authenticateWithPassword(PasswordLoginRequestDTO request, Member member) {
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            log.warn("이메일: {} 의 비밀번호가 일치하지 않습니다.", member.getEmail());
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private String authenticateWithPasskey(PasskeyLoginRequestDTO request, Member member) {

        log.info("이메일: {} 의 Passkey challenge 생성 중", member.getEmail());
        return webAuthnService.generateChallenge(member.getMemberId());
    }

    private void authenticateWithMfa(MfaLoginRequestDTO request, Member member) {
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            log.warn("이메일: {} 의 MFA 인증을 위한 비밀번호가 일치하지 않습니다.", member.getEmail());
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        MfaType mfaType = request.mfaType();
        createMfaLog(request, member, mfaType);
    }

    private LoginResponseDTO generateTokens(Member member, String challenge) {
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));
        redisTemplate.opsForValue().set("refreshToken:" + member.getMemberId(), refreshToken);

        // 리프레시 토큰 검증 추가
        String storedToken = redisTemplate.opsForValue().get("refreshToken:" + member.getMemberId());

        log.info("저장된 리프레시 토큰: {}", storedToken);
        log.info("전달된 리프레시 토큰: {}", refreshToken);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            log.warn("유효하지 않은 리프레시 토큰입니다.");
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String accessToken = tokenService.createNewAccessToken(refreshToken);

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .challenge(challenge)
                .build();
    }

    private void createMfaLog(MfaLoginRequestDTO request, Member member, MfaType mfaType) {
        log.info("이메일: {} 의 MFA 로그 생성 중", member.getEmail());

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
        log.info("회원 ID: {} 의 로그아웃 처리 중", memberId);
        redisTemplate.delete("refreshToken:" + memberId);
    }
}