package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.application.AuthService;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.enums.LoanType;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.AnalyzeConsumptionTypeResponse;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupPasswordRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final DummyFinancialDataGenerator dummyFinancialDataGenerator;
    private final LoanApplicationRepository loanApplicationRepository;
    private final AuthService authService;

    // 비밀번호 기반 회원가입
    @Transactional
    public SignupResponseDTO registerByPassword(SignupPasswordRequestDTO dto) {
        if (memberRepository.existsByEmail(dto.email())) {
            log.warn("이미 등록된 이메일로 회원가입 시도:\nemail={}", dto.email());
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
                .creditScoreEvaluated(false)
                .build();

        Member saved = memberRepository.save(member);
        log.info("회원 DB 저장 성공:\nmemberId={}, email={}", saved.getMemberId(), saved.getEmail());

        authService.registerPin(dto.pin(), saved.getMemberId());
        log.info("회원 PIN 등록 완료:\nmemberId={}", saved.getMemberId());

        dummyFinancialDataGenerator.generateDummyFinancialData(saved);
        log.info("회원 금융 데이터 생성 완료:\nmemberId={}", saved.getMemberId());

        LoanApplication application = LoanApplication.builder()
                .member(member)
                .status(LoanApplicationStatus.NONE)
                .loanType(LoanType.NEW)
                .loanTransactions(new ArrayList<>())
                .interests(new ArrayList<>())
                .build();

        loanApplicationRepository.save(application);
        log.info("회원 대출 신청 객체 생성 완료:\nmemberId={}, applicationId={}", saved.getMemberId(), application.getApplicationId());

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .build();
    }

    /**
     * FIDO(패스키) 등록
     * @param memberId 회원 ID
     * @param dto 패스키 등록 요청 정보
     */
    @Transactional
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

        log.info("소비타입 분석 결과:\nconsumptionType={}", randomType);

        return AnalyzeConsumptionTypeResponse.builder()
                .consumptionType(randomType)
                .build();
    }

}