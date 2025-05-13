package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.application.TokenService;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.domain.jwt.RefreshToken;
import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

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
        log.debug("패스워드 검증 시작: 이메일: {}", member.getEmail());

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            log.warn("이메일: {} 의 비밀번호가 일치하지 않습니다.", member.getEmail());
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.debug("패스워드 검증 성공: 이메일: {}", member.getEmail());
    }

    private String authenticateWithPasskey(PasskeyLoginRequestDTO request, Member member) {
        log.info("이메일: {} 의 Passkey challenge 생성 중", member.getEmail());
        String challenge = webAuthnService.generateChallenge(member.getMemberId());
        log.debug("Passkey challenge 생성 완료: 이메일: {}, challenge: {}", member.getEmail(), challenge);
        return challenge;
    }

    private void authenticateWithMfa(MfaLoginRequestDTO request, Member member) {
        MfaType mfaType = request.mfaType();
        log.info("이메일: {} 의 MFA 타입: {}", member.getEmail(), mfaType);
        createMfaLog(request, member, mfaType);
    }

    // DB에 리프레시 토큰 저장
    private void saveRefreshTokenToDb(Long memberId, String refreshToken) {
        RefreshToken token = new RefreshToken(memberId, refreshToken);
        refreshTokenRepository.save(token);  // DB에 리프레시 토큰 저장
        log.info("DB에 리프레시 토큰 저장 완료: {}", refreshToken);
    }

    // generateTokens 메서드에서 리프레시 토큰 저장
    private LoginResponseDTO generateTokens(Member member, String challenge) {
        // Refresh Token 발급
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(7));

        // Redis에 refreshToken 저장 (7일 만료)
        String redisKey = "refreshToken:" + member.getMemberId();
        redisTemplate.opsForValue().set(redisKey, refreshToken, Duration.ofDays(7));
        log.info("리프레시 토큰 저장 완료: refreshToken: {}", refreshToken);

        // DB에 리프레시 토큰도 저장
        saveRefreshTokenToDb(member.getMemberId(), refreshToken);

        // Redis에서 저장된 refreshToken 조회
        String storedRefreshToken = redisTemplate.opsForValue().get(redisKey);

        if (storedRefreshToken == null) {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        log.info("저장된 리프레시 토큰 조회: {}", storedRefreshToken);

        // Access Token 발급
        String accessToken = tokenService.createNewAccessToken(storedRefreshToken);
        log.info("엑세스 토큰 발급 완료: {}", accessToken);

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
        log.info("MFA 로그 저장 완료: 이메일: {}, MFA 타입: {}", member.getEmail(), mfaType);
    }

    public void logout(Long memberId) {
        log.info("회원 ID: {} 의 로그아웃 처리 시작", memberId);
        redisTemplate.delete("refreshToken:" + memberId);
        log.info("회원 ID: {} 의 로그아웃 처리 완료", memberId);
    }
}