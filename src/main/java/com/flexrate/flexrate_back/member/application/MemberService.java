package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.*;
import com.flexrate.flexrate_back.member.enums.*;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원 ID로 회원 조회
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));
    }

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