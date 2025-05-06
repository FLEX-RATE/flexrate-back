package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.application.TokenService;
import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.auth.domain.jwt.RefreshToken;
import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.api.WebAuthnService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final WebAuthnService webAuthnService;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder; // 추가

    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        Member member = memberRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String accessToken;
        String refreshTokenValue;
        Optional<FidoCredential> fidoCredentialOpt = Optional.empty();

        switch (requestDTO.method()) {
            case PASSWORD:
                if (!passwordEncoder.matches(requestDTO.password(), member.getPasswordHash())) {
                    throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
                }
                accessToken = tokenService.createNewAccessToken(requestDTO.email());
                break;

            case PASSKEY:
                fidoCredentialOpt = webAuthnService.authenticatePasskey(member.getMemberId(), requestDTO.passkeyData());
                if (fidoCredentialOpt.isEmpty()) {
                    throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
                }
                accessToken = tokenService.createNewAccessToken(requestDTO.email());
                break;

            case SOCIAL:
                accessToken = tokenService.createNewAccessToken(requestDTO.email());
                break;

            default:
                throw new FlexrateException(ErrorCode.VALIDATION_ERROR);
        }

        refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = new RefreshToken(member.getMemberId(), refreshTokenValue);
        refreshTokenRepository.findByMemberId(member.getMemberId())
                .ifPresentOrElse(
                        existing -> refreshTokenRepository.save(existing.update(refreshTokenValue)),
                        () -> refreshTokenRepository.save(newRefreshToken)
                );

        return new LoginResponseDTO(
                member.getMemberId(),
                accessToken,
                refreshTokenValue,
                member.getEmail(),
                member.getName(),
                member.getSex(),
                member.getConsumptionType(),
                member.getConsumeGoal(),
                fidoCredentialOpt.map(f -> List.of(f.getPublicKey())).orElse(null)
        );
    }
}
