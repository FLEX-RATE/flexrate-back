package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.*;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.Sex;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final DummyFinancialDataGenerator dummyFinancialDataGenerator;

    // 1. 비밀번호 기반 회원가입
    public SignupResponseDTO registerByPassword(SignupPasswordRequestDTO dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String hashedPwd = passwordEncoder.encode(dto.password());

        Member member = Member.builder()
                .email(dto.email())
                .passwordHash(hashedPwd)
                .name(dto.name())
                .sex(dto.sex())
                .birthDate(dto.birthDate())
                .consumptionType(dto.consumptionType())
                .consumeGoal(dto.consumeGoal())
                .status(MemberStatus.ACTIVE)
                .role(Role.MEMBER)
                .build();

        Member saved = memberRepository.save(member);
        handleConsents(dto.consents());
        dummyFinancialDataGenerator.generateDummyFinancialData(saved);

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .build();
    }

    // 2. 패스키 기반 회원가입
    public SignupResponseDTO registerByPasskey(SignupPasskeyDTO dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        Member member = Member.builder()
                .email(dto.email())
                .passwordHash(null) // 패스워드 없음
                .name(dto.name())
                .sex(dto.sex())
                .birthDate(dto.birthDate())
                .consumptionType(dto.consumptionType())
                .consumeGoal(dto.consumeGoal())
                .status(MemberStatus.ACTIVE)
                .role(Role.MEMBER)
                .build();

        Member saved = memberRepository.save(member);

        if (dto.passkeys() != null && !dto.passkeys().isEmpty()) {
            registerPasskeys(saved, dto.passkeys());
        }

        handleConsents(dto.consents());
        dummyFinancialDataGenerator.generateDummyFinancialData(saved);

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .build();
    }

    private void registerPasskeys(Member member, List<PasskeyRequestDTO> passkeys) {
        for (PasskeyRequestDTO passkey : passkeys) {
            String challenge = webAuthnService.generateChallenge(member.getMemberId());

            if (!webAuthnService.verifySignatureForRegistration(
                    passkey.publicKey(), challenge, passkey.credentialId())) {
                throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
            }

            webAuthnService.registerPasskey(member, passkey);
        }
    }

    private void handleConsents(List<ConsentRequestDTO> consents) {
        if (consents == null) return;
        for (ConsentRequestDTO consent : consents) {
            log.info("Consent type: {} , Agreed: {}", consent.type(), consent.agreed());
        }
    }
}