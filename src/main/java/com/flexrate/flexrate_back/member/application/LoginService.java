package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.MfaLog;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MfaLogRepository;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final FidoCredentialRepository fidoCredentialRepository;
    private final MfaLogRepository mfaLogRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. 이메일로 회원 정보 조회
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 2. 인증 방식 처리
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
                authenticatePasskey(member, request.passkeyData());
            }
            default -> throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(14));

        // 4. 응답 반환
        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void createMfaLog(LoginRequestDTO request, Member member) {
        MfaLog mfaLog = MfaLog.builder()
                .mfaType(request.mfaType())
                .result(request.mfaResult())
                .authenticatedAt(LocalDateTime.now())
                .deviceInfo(request.deviceInfo())
                .transaction(null) // 추후 transactionId 받으면 처리
                .build();

        mfaLogRepository.save(mfaLog);
    }

    private void authenticatePasskey(Member member, String passkeyData) {
        Optional<FidoCredential> credentials = fidoCredentialRepository.findByMember_MemberId(member.getMemberId());

        boolean match = credentials.stream()
                .anyMatch(cred -> cred.isActive() && cred.getPublicKey().equals(passkeyData));

        if (!match) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }
    }

    private String getRegisteredPasskeys(Member member) {
        return fidoCredentialRepository.findByMember_MemberId(member.getMemberId()).stream()
                .filter(FidoCredential::isActive)
                .map(FidoCredential::getDeviceInfo)
                .collect(Collectors.joining(", "));
    }
}
