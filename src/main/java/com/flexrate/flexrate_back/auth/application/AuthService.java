package com.flexrate.flexrate_back.auth.application;

import com.flexrate.flexrate_back.auth.domain.PinCredential;
import com.flexrate.flexrate_back.auth.domain.repository.PinCredentialRepository;
import com.flexrate.flexrate_back.auth.dto.PinRequest;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {
    private final PinCredentialRepository pinCredentialRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인된 사용자의 PIN 등록 여부 확인
     * @param member 로그인된 사용자 정보
     */
    public boolean checkPinRegistered(Member member) {
        boolean present = pinCredentialRepository.findByMember_MemberId(member.getMemberId()).isPresent();
        log.debug("PIN 등록 여부: memberId={}, registered={}", member.getMemberId(), present);

        return present;
    }

    /**
     * PIN 등록
     * @param pin PIN 등록 요청 정보
     * @param userId 로그인된 사용자 ID
     */
    @Transactional
    public void registerPin(String pin, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        if (pinCredentialRepository.findByMember_MemberId(member.getMemberId()).isPresent()) {
            log.warn("PIN 이미 등록된 사용자: memberId={}", member.getMemberId());
            throw new FlexrateException(ErrorCode.PIN_ALREADY_REGISTERED);
        }

        String hashedPin = passwordEncoder.encode(pin);
        PinCredential pinCredential = PinCredential.builder()
                .member(member)
                .pinHash(hashedPin)
                .build();

        pinCredentialRepository.save(pinCredential);
    }

    /**
     * PIN 인증
     * @param pinRequest PIN 인증 요청 정보
     * @param member 로그인된 사용자 정보
     */
    public boolean verifyPin(PinRequest pinRequest, Member member) {
        PinCredential pinCredential = pinCredentialRepository.findByMember_MemberId(member.getMemberId())
                .orElseThrow(() -> {
                    log.warn("PIN 인증 실패: 등록되지 않은 사용자 PIN, memberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.PIN_NOT_REGISTERED);
                });

        boolean isValid = passwordEncoder.matches(pinRequest.pin(), pinCredential.getPinHash());
        log.debug("PIN 인증 결과: memberId={}, isValid={}", member.getMemberId(), isValid);

        return isValid;
    }
}
