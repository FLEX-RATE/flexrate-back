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
     * @param passkeyData 패스키 인증 데이터 (예: WebAuthn 인증 데이터)
     * @return 인증 성공 여부
     * @throws FlexrateException 인증 실패 시 예외 발생
     */
    public Optional<FidoCredential> authenticatePasskey(Long userId, String passkeyData) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        Optional<FidoCredential> fidoCredentialOptional = fidoCredentialRepository.findByMember_MemberId(userId);

        if (fidoCredentialOptional.isEmpty()) {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        FidoCredential fidoCredential = fidoCredentialOptional.get();
        if ("valid".equals(passkeyData)) {
            return Optional.of(fidoCredential);
        } else {
            return Optional.empty();
        }
    }
}