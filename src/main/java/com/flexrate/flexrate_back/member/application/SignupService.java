package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.AnalyzeConsumptionTypeResponse;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupPasswordRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final DummyFinancialDataGenerator dummyFinancialDataGenerator;

    // 비밀번호 기반 회원가입
    public SignupResponseDTO registerByPassword(SignupPasswordRequestDTO dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String hashedPwd = passwordEncoder.encode(dto.password());

        Member member = Member.builder()
                .age(Period.between(dto.birthDate(), LocalDate.now()).getYears())
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
        dummyFinancialDataGenerator.generateDummyFinancialData(saved);

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .build();
    }

    public void addFidoCredential(Long memberId, PasskeyRequestDTO dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        webAuthnService.registerPasskey(member, dto);
    }

    public String generateFidoChallenge(Long memberId) {
        return webAuthnService.generateChallenge(memberId);
    }

    // 임시 소비성향 도출 메서드
    public AnalyzeConsumptionTypeResponse analyzeConsumptionType() {
        ConsumptionType[] types = ConsumptionType.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(types.length);
        ConsumptionType randomType = types[randomIndex];

        return AnalyzeConsumptionTypeResponse.builder()
                .consumptionType(randomType)
                .build();
    }

}