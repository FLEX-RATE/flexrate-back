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
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.repository.MfaLogRepository;
import com.flexrate.flexrate_back.member.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

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
    private final FidoCredentialRepository fidoCredentialRepository;

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
        stringRedisUtil.set(redisKey, String.valueOf(member.getMemberId()), Duration.ofDays(7));

        return LoginResponseDTO.builder()
                .userId(member.getMemberId())
                .username(member.getName())
                .email(member.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .challenge("")
                .build();
    }

    public PasskeyLoginChallengeResponseDTO generateLoginChallenge(PasskeyLoginChallengeRequestDTO request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String challenge = webAuthnService.generateChallenge(member.getMemberId());

        List<FidoCredential> credentials = fidoCredentialRepository.findAllByMember_MemberIdAndIsActiveTrue(member.getMemberId());

        List<String> allowedCredentialIds = credentials.stream()
                .map(FidoCredential::getCredentialKey) // ← credentialId ❌ → credentialKey ✅
                .toList();


        return PasskeyLoginChallengeResponseDTO.builder()
                .challenge(challenge)
                .rpId("flexrate.com")
                .userHandle(member.getMemberId().toString())
                .allowedCredentialIds(allowedCredentialIds)
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
                .challenge("")
                .build();
    }



    public PasskeyChallengeResponseDTO generatePasskeyRegistrationChallenge(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        Long memberId = member.getMemberId();
        String challenge = webAuthnService.generateChallenge(memberId);

        List<String> pubKeyCredParams = List.of("{\"type\":\"public-key\",\"alg\":-7}");

        return new PasskeyChallengeResponseDTO(
                challenge,
                "flexrate.com",        // relyingPartyId
                "Flexrate",             // relyingPartyName
                memberId.toString(),    // userId
                member.getEmail(),      // userName
                member.getEmail(),      // userDisplayName
                60000L,                 // timeout (ms)
                pubKeyCredParams,
                "preferred",            // userVerification
                true                    // residentKey
        );
    }

    /**
     * 패스키 로그인용 Challenge 생성 (로그인 시 사용)
     */
    public PasskeyLoginChallengeResponseDTO generatePasskeyLoginChallenge(PasskeyLoginChallengeRequestDTO request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String challenge = webAuthnService.generateChallenge(member.getMemberId());

        // FidoCredentialRepository로 memberId 기준 조회
        List<FidoCredential> credentials = fidoCredentialRepository.findAllByMember_MemberIdAndIsActiveTrue(member.getMemberId());

        List<String> allowedCredentialIds = credentials.stream()
                .map(FidoCredential::getCredentialKey) // ← credentialId ❌ → credentialKey ✅
                .toList();

        return PasskeyLoginChallengeResponseDTO.builder()
                .challenge(challenge)
                .rpId("flexrate.com")
                .userHandle(member.getMemberId().toString())
                .allowedCredentialIds(allowedCredentialIds)
                .build();
    }


    public LoginResponseDTO loginWithMFA(MfaLoginRequestDTO request) {
        log.info("MFA 로그인 시도 memberId={}", request.memberId());

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> {
                    log.warn("MFA 로그인: 존재하지 않는 사용자 memberId={}", request.memberId());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        PasskeyAuthenticationDTO passkeyAuthDTO = PasskeyAuthenticationDTO.builder()
                .clientDataJSON(request.clientDataJSON())
                .authenticatorData(request.authenticatorData())
                .signature(request.signature())
                .build();

        FidoCredential credential;
        AuthResult authResult;
        try {
            credential = webAuthnService
                    .authenticatePasskey(member.getMemberId(), passkeyAuthDTO, request.challenge())
                    .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));
            authResult = AuthResult.SUCCESS;
        } catch (FlexrateException e) {
            authResult = AuthResult.FAILURE;
            // 인증 실패 로그 저장
            MfaLog failLog = MfaLog.builder()
                    .mfaType(MfaType.FIDO2)
                    .authenticatedAt(LocalDateTime.now())
                    .result(authResult)
                    .deviceInfo(request.deviceInfo())
                    .build();
            mfaLogRepository.save(failLog);
            throw e;  // 인증 실패 예외 다시 던짐
        }

        // 인증 성공 로그 저장
        MfaLog successLog = MfaLog.builder()
                .mfaType(MfaType.FIDO2)
                .authenticatedAt(LocalDateTime.now())
                .result(authResult)
                .deviceInfo(request.deviceInfo())
                .build();
        mfaLogRepository.save(successLog);

        // 토큰 발급 및 Redis 저장 등 후속 처리
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