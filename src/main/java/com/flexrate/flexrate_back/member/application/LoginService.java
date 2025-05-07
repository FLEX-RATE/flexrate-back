package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final FidoCredentialRepository fidoCredentialRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequestDTO request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        switch (request.authMethod()) {
            case "MFA" -> {
                if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
                    throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
                }
            }
            case "FIDO" -> {
                authenticatePasskey(member, request.passkeyData());
            }
            default -> throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));
        String refreshToken = jwtTokenProvider.generateToken(member, Duration.ofDays(14));

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .passkeyList(getRegisteredPasskeys(member))
                .build();
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