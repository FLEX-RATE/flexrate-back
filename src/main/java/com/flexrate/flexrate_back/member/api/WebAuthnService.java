package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class WebAuthnService {

    private final MemberRepository memberRepository;

    public WebAuthnService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 패스키 인증을 처리하는 메서드
     * @param userId 사용자 ID
     * @param passkeyData 패스키 인증 데이터 (예: WebAuthn 인증 데이터)
     * @return 인증 성공 여부
     * @throws FlexrateException 인증 실패 시 예외 발생
     */
    public boolean authenticatePasskey(Long userId, String passkeyData) {
        // 유저 정보 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        // 여기서 passkeyData를 이용한 실제 인증 처리 로직을 구현해야 합니다.
        // WebAuthn 라이브러리를 사용하여 passkeyData로 인증을 처리합니다.
        // 예를 들어, passkeyData는 WebAuthn 응답 데이터를 포함하고 있어야 합니다.

        if ("valid".equals(passkeyData)) {
            return true;
        } else {
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }
    }
}
