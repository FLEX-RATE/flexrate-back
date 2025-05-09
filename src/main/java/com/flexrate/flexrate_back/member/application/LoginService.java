package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.api.WebAuthnService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
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
    private final FidoCredentialRepository fidoCredentialRepository;
    private final MfaLogRepository mfaLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final RedisTemplate<String, String> redisTemplate;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        switch (request.authMethod()) {
            case MFA -> {
                if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
                    throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
                }

                createMfaLog(request, member);
            }
            case FIDO -> {
                if (request.passkeyData() == null || request.passkeyData().isEmpty()) {
                    throw new FlexrateException(ErrorCode.AUTHENTICATION_REQUIRED);
                }

                // Challenge 발급
                String challenge = webAuthnService.generateChallenge(member.getMemberId());
                // 클라이언트에게 challenge 전송 후 패스키 응답 검증
                webAuthnService.authenticatePasskey(member.getMemberId(), request.passkeyData(), challenge)
                        .orElseThrow(() -> new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED));
            }
            default -> throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(14));

        // 4. refreshToken Redis에 저장
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("refreshToken:" + member.getMemberId(), refreshToken, Duration.ofDays(14));

        // 5. 응답 반환
        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 로그아웃 처리 (Redis에서 refreshToken 삭제)
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

    // refreshToken 블랙리스트 관리
    public void blacklistRefreshToken(Long memberId) {
        String refreshToken = redisTemplate.opsForValue().get("refreshToken:" + memberId);

        if (refreshToken != null) {
            redisTemplate.opsForValue().set("blacklist:refreshToken:" + memberId, refreshToken, Duration.ofDays(14));
            redisTemplate.delete("refreshToken:" + memberId);
        } else {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }

    private void createMfaLog(LoginRequestDTO request, Member member) {
        MfaLog mfaLog = MfaLog.builder()
                .mfaType(request.mfaType())
                .result(request.mfaResult())
                .authenticatedAt(LocalDateTime.now())
                .deviceInfo(request.deviceInfo())
                .transaction(null)
                .build();

        mfaLogRepository.save(mfaLog);
    }
}