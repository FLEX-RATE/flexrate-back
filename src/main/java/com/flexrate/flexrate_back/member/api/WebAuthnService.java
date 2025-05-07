package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebAuthnService {

    private final MemberRepository memberRepository;
    private final FidoCredentialRepository fidoCredentialRepository;

    public WebAuthnService(MemberRepository memberRepository, FidoCredentialRepository fidoCredentialRepository) {
        this.memberRepository = memberRepository;
        this.fidoCredentialRepository = fidoCredentialRepository;
    }

    /**
     * 패스키 인증을 처리하는 메서드
     * @param userId 사용자 ID
     * @param passkeyData WebAuthn 인증 데이터 (예: attestationObject, clientDataJSON)
     * @return 인증 성공 여부
     * @throws FlexrateException 인증 실패 시 예외 발생
     */
    public Optional<FidoCredential> authenticatePasskey(Long userId, String passkeyData) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // WebAuthn 인증 데이터 검증 로직을 추가합니다.
        boolean isValid = validatePasskeyData(passkeyData);
        if (!isValid) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        Optional<FidoCredential> fidoCredentialOptional = fidoCredentialRepository.findByMember_MemberId(userId);
        return fidoCredentialOptional;
    }

    /**
     * WebAuthn 인증 데이터 검증
     * @param passkeyData WebAuthn 인증 데이터
     * @return 유효한 데이터인지 여부
     */
    private boolean validatePasskeyData(String passkeyData) {
        // WebAuthn 인증 데이터를 검증하는 로직을 추가해야 합니다.
        // 실제 인증 데이터 검증 과정 (예: attestationObject, clientDataJSON 검증 등)을 수행합니다.
        return "valid".equals(passkeyData);  // 예시로 "valid"일 경우만 인증 성공
    }

    // 패스키 등록 및 저장
    public void registerPasskey(Member member, String publicKey, String signCount, String deviceInfo) {
        FidoCredential fidoCredential = FidoCredential.builder()
                .member(member)
                .publicKey(publicKey)
                .signCount(signCount)
                .deviceInfo(deviceInfo)
                .isActive(true)
                .build();

        fidoCredentialRepository.save(fidoCredential);
    }
}
