package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
<<<<<<< HEAD
import com.flexrate.flexrate_back.member.dto.ConsentRequestDTO;
import com.flexrate.flexrate_back.member.dto.PasskeyRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
=======
import com.flexrate.flexrate_back.member.dto.*;
>>>>>>> cadeee6ba46e06d63f681b4611b1408ab662b6d6
import com.flexrate.flexrate_back.member.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final FidoCredentialRepository fidoCredentialRepository;

    /*
     * 회원가입 중복 이메일을 체크하고, 회원을 등록한 후, 생성된 회원 정보를 응답
     * @since 2025.05.03
     * @author 윤영찬
     * */
    public SignupResponseDTO registerMember(SignupRequestDTO signupDTO) {
        if (memberRepository.existsByEmail(signupDTO.email())) {
            throw new FlexrateException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        String rawPwd = signupDTO.password();
        String hashedPwd = passwordEncoder.encode(rawPwd);

        ConsumptionType consumptionType;
        ConsumeGoal consumeGoal;
        Sex sex = Sex.valueOf(signupDTO.sex());

        try {
            consumptionType = signupDTO.consumptionType();
            consumeGoal = signupDTO.consumeGoal();
        } catch (IllegalArgumentException e) {
            throw new FlexrateException(ErrorCode.VALIDATION_ERROR);
        }

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

        Member saved = memberRepository.save(member);

        if (signupDTO.passkeys() != null && !signupDTO.passkeys().isEmpty()) {
            savePasskeys(saved, signupDTO.passkeys());
        }

        if (signupDTO.consents() != null && !signupDTO.consents().isEmpty()) {
            handleConsents(signupDTO.consents());
        }

        return SignupResponseDTO.builder()
                .userId(saved.getMemberId())
                .email(saved.getEmail())
                .build();
    }

    private void savePasskeys(Member member, List<PasskeyRequestDTO> passkeys) {
        for (PasskeyRequestDTO passkey : passkeys) {
            FidoCredential fidoCredential = FidoCredential.builder()
                    .member(member)
                    .publicKey(passkey.publicKey())
                    .signCount(passkey.signCount())
                    .deviceInfo(passkey.deviceInfo())
                    .isActive(passkey.isActive())
                    .lastUsedDate(LocalDateTime.now())
                    .build();

            fidoCredentialRepository.save(fidoCredential);
        }
    }

    private void handleConsents(List<ConsentRequestDTO> consents) {
        for (ConsentRequestDTO consent : consents) {
            // 실제로 consents를 DB에 저장하거나 후속 작업을 수행할 수 있습니다.
            // 이 예시에서는 단순히 출력만 합니다.
            System.out.println("Consent type: " + consent.type() + ", Agreed: " + consent.agreed());
        }
    }




    // 회원 ID로 회원 조회
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));
    }
<<<<<<< HEAD
}
=======

    /**
     * 마이페이지 조회
     * @param memberId 회원 ID
     * @return 회원 정보(MypageResponse) - 이름, 이메일, 소비 목표, 소비 유형
     * @since 2025.05.07
     * @author 권민지
     */
    public MypageResponse getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        return MypageResponse.builder()
                .name(member.getName())
                .email(member.getEmail())
                .consumeGoal(member.getConsumeGoal())
                .consumptionType(member.getConsumptionType())
                .build();
    }

    /**
     * 마이페이지 정보 수정
     * @param memberId 회원 ID
     * @param request MypageUpdateRequest 요청 DTO (이메일, 소비 목표)
     * @return 수정된 회원 정보(MypageResponse) - 이름, 이메일, 소비 목표, 소비 유형
     * @since 2025.05.07
     * @author 권민지
     */
    @Transactional
    public MypageResponse updateMyPage(Long memberId, MypageUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        if (request.email() != null) member.updateEmail(request.email());
        if (request.consumeGoal() != null) {
            // L011 - 소비 목표가 소비 타입과 다르다면, 소비 목표를 변경할 수 없음
            if (request.consumeGoal().getType() != member.getConsumptionType()) {
                throw new FlexrateException(ErrorCode.LOAN_CONSUMPTION_TYPE_MISMATCH);
            }

            member.updateConsumeGoal(request.consumeGoal());
        }

        return MypageResponse.builder()
                .name(member.getName())
                .email(member.getEmail())
                .consumeGoal(member.getConsumeGoal())
                .consumptionType(member.getConsumptionType())
                .build();
    }

    /**
     * 소비 유형별 소비 목표 반환
     * @param consumptionType 소비 유형
     * @return 소비 목표 list
     * @since 2025.05.07
     * @author 권민지
     */
    public ConsumeGoalResponse getConsumeGoal(ConsumptionType consumptionType) {
        return ConsumeGoalResponse.builder()
                .consumeGoals(ConsumeGoal.getConsumeGoalsByType(consumptionType))
                .build();

    }
}

>>>>>>> cadeee6ba46e06d63f681b4611b1408ab662b6d6
