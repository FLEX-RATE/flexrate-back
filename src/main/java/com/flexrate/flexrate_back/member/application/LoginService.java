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
import com.flexrate.flexrate_back.member.dto.PasskeyAuthenticationDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final MemberRepository memberRepository;
    private final MfaLogRepository mfaLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final RedisTemplate<String, String> redisTemplate;

    // 로그인 방식에 따른 인증 처리
    public LoginResponseDTO login(LoginRequestDTO request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String challenge = null;

        switch (request.loginMethod()) {
            case PASSWORD -> {
                authenticateWithPassword(request, member);
                // 패스워드 인증에서 챌린지가 필요하지 않으니 null
                challenge = "";
            }
            case PASSKEY -> challenge = authenticateWithPasskey(request, member);
            default -> throw new FlexrateException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        // 인증 방식에 따른 추가 인증 처리
        switch (request.authMethod()) {
            case MFA -> authenticateWithMfa(request, member);
            case FIDO -> challenge = authenticateWithFido(request, member);
            default -> throw new FlexrateException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        // 토큰 생성 및 반환
        return generateTokens(member, challenge);
    }

    private void authenticateWithPassword(LoginRequestDTO request, Member member) {
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            logger.warn("Invalid credentials for user {}", member.getEmail());
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private String authenticateWithPasskey(LoginRequestDTO request, Member member) {
        // Passkey 인증 후 challenge 생성
        return webAuthnService.generateChallenge(member.getMemberId());
    }

    private void authenticateWithMfa(LoginRequestDTO request, Member member) {
        // MFA 인증 처리
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            logger.warn("Invalid credentials for MFA for user {}", member.getEmail());
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        // MFA 인증 로그 생성
        MfaType mfaType = request.mfaType();
        createMfaLog(request, member, mfaType);
    }

    private String authenticateWithFido(LoginRequestDTO request, Member member) {
        // 클라이언트가 보내온 챌린지
        String clientChallenge = request.challenge();
        if (clientChallenge == null || clientChallenge.isEmpty()) {
            // 클라이언트가 초기 인증을 요청한 경우, 새로운 챌린지 발급
            return webAuthnService.generateChallenge(member.getMemberId());
        }

        // Redis에서 저장된 챌린지 조회
        String redisKey = "fido:challenge:" + member.getMemberId();
        String savedChallenge = redisTemplate.opsForValue().get(redisKey);

        if (savedChallenge == null || !savedChallenge.equals(clientChallenge)) {
            logger.warn("Challenge mismatch during FIDO authentication for user {}", member.getEmail());
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        // Challenge 서명 검증
        PasskeyAuthenticationDTO passkeyDTO = request.passkeyData();
        boolean isValid = webAuthnService.authenticatePasskey(member.getMemberId(), passkeyDTO, clientChallenge).isPresent();
        if (!isValid) {
            logger.warn("Invalid passkey authentication for user {}", member.getEmail());
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        // 챌린지 삭제 (보안 강화)
        redisTemplate.delete(redisKey);

        // 인증 성공 시, 받은 challenge를 반환
        return clientChallenge;
    }

    private LoginResponseDTO generateTokens(Member member, String challenge) {
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

    // 로그아웃: 리프레시 토큰 삭제
    public void logout(Long memberId) {
        redisTemplate.delete("refreshToken:" + memberId); // Redis에서 refreshToken 삭제
    }

    // 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
    public String refreshAccessToken(Long memberId) {
        String refreshToken = redisTemplate.opsForValue().get("refreshToken:" + memberId);

        if (refreshToken == null) {
            logger.warn("Invalid refresh token request for memberId {}", memberId);
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        return jwtTokenProvider.generateToken(member, Duration.ofHours(2));
    }

    // 리프레시 토큰 블랙리스트에 추가
    public void blacklistRefreshToken(Long memberId) {
        String refreshToken = redisTemplate.opsForValue().get("refreshToken:" + memberId);

        if (refreshToken != null) {
            redisTemplate.opsForValue().set("blacklist:refreshToken:" + memberId, refreshToken, Duration.ofDays(14));
            redisTemplate.delete("refreshToken:" + memberId);
        } else {
            logger.warn("Refresh token not found for blacklist for memberId {}", memberId);
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}
