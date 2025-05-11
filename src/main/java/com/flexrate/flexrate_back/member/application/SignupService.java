package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.ConsentRequestDTO;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.Sex;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SignupService {

    private static final Logger logger = LoggerFactory.getLogger(SignupService.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebAuthnService webAuthnService;
    private final DummyFinancialDataGenerator dummyFinancialDataGenerator;

    /*
     * 회원가입 중복 이메일을 체크하고, 회원을 등록한 후, 생성된 회원 정보를 응답
     * @since 2025.05.03
     * @author 윤영찬
     * */
    public SignupResponseDTO registerMember(SignupRequestDTO signupDTO) {
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(signupDTO.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        // 비밀번호 암호화
        String rawPwd = signupDTO.password();
        String hashedPwd = passwordEncoder.encode(rawPwd);

        // 회원의 소비 유형 및 목표 검증
        ConsumptionType consumptionType;
        ConsumeGoal consumeGoal;
        Sex sex = signupDTO.sex();

        try {
            consumptionType = signupDTO.consumptionType();
            consumeGoal = signupDTO.consumeGoal();
        } catch (IllegalArgumentException e) {
            throw new FlexrateException(ErrorCode.VALIDATION_ERROR);
        }

        // 회원 정보 생성
        Member member = Member.builder()
                .email(signupDTO.email())
                .passwordHash(hashedPwd)
                .name(signupDTO.name())
                .sex(sex)
                .birthDate(signupDTO.birthDate())
                .status(MemberStatus.ACTIVE)
                .consumptionType(consumptionType)
                .consumeGoal(consumeGoal)
                .role(Role.MEMBER)
                .build();

        // 회원 저장
        Member saved = memberRepository.save(member);

        // 패스키 등록 처리 (패스키가 있는 경우에만)
        if (signupDTO.passkeys() != null && !signupDTO.passkeys().isEmpty()) {
            registerPasskeys(saved, signupDTO.passkeys());
        }

        // 동의 사항 처리 (있는 경우에만)
        if (signupDTO.consents() != null && !signupDTO.consents().isEmpty()) {
            handleConsents(signupDTO.consents());
        }

        // 더미 금융 데이터 생성
        dummyFinancialDataGenerator.generateDummyFinancialData(saved);

        // 응답 객체 반환
        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .build();
    }

    // 패스키 등록 및 challenge 생성
    private void registerPasskeys(Member member, List<PasskeyRequestDTO> passkeys) {
        for (PasskeyRequestDTO passkey : passkeys) {
            try {
                // WebAuthn 인증 challenge 생성
                String challenge = webAuthnService.generateChallenge(member.getMemberId());

                // 서명 검증 로직 호출 (가입 시에도 서명 검증)
                if (!webAuthnService.verifySignatureForRegistration(
                        passkey.publicKey(), challenge, passkey.credentialId())) {
                    throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
                }

                // 패스키 등록 처리
                webAuthnService.registerPasskey(member, passkey);

            } catch (FlexrateException e) {
                logger.error("패스키 등록 중 오류 (회원ID: {}): {}", member.getMemberId(), e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("패스키 등록 중 예상치 못한 오류 (회원ID: {})", member.getMemberId(), e);
                throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED, e);
            }
        }
    }

    // 동의 사항 처리
    private void handleConsents(List<ConsentRequestDTO> consents) {
        for (ConsentRequestDTO consent : consents) {
            // 동의 사항의 타입과 동의 여부 로깅
            logger.info("Consent type: {} , Agreed: {}", consent.type(), consent.agreed());
        }
    }
}
