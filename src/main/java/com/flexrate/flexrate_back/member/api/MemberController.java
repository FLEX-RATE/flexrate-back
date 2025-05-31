package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.auth.resolver.CurrentMemberId;
import com.flexrate.flexrate_back.loan.dto.MainPageResponse;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.dto.ConsumeGoalResponse;
import com.flexrate.flexrate_back.member.dto.CreditScoreStatusResponse;
import com.flexrate.flexrate_back.member.dto.MypageResponse;
import com.flexrate.flexrate_back.member.dto.MypageUpdateRequest;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    /**
     * 메인페이지 조회
     * @return 마이페이지(MainPageResponse)
     * @since 2025.05.24
     * @author 유승한
     */
    @Operation(summary = "로그인한 사용자의 메인페이지 조회",
            description = "로그인한 사용자의 메인페이지 정보를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "사용자의 마이페이지 조회 결과 반환"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.")})
    @GetMapping("/main")
    public ResponseEntity<MainPageResponse> getMainPage(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(memberService.getMainPage(memberId));
    }

    /**
     * 마이페이지 조회
     * @return 회원 정보(MypageResponse) - 이름, 이메일, 소비 목표, 소비 유형
     * @since 2025.05.07
     * @author 권민지
     */
    @Operation(summary = "로그인한 사용자의 마이페이지 조회",
            description = "로그인한 사용자의 마이페이지 정보를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "사용자의 마이페이지 조회 결과 반환"),
                         @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.")})
    @GetMapping("/mypage")
    public ResponseEntity<MypageResponse> getMyPage(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(memberService.getMyPage(memberId));
    }

    /**
     * 마이페이지 정보 수정
     * @param request 마이페이지 수정 요청
     * @return 회원 정보(MypageResponse) - 이름, 이메일, 소비 목표, 소비 유형
     * @since 2025.05.07
     * @author 권민지
     */
    @Operation(summary = "로그인한 사용자의 마이페이지 정보 수정",
            description = "로그인한 사용자의 마이페이지 정보를 수정합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "사용자의 마이페이지 수정 결과 반환"),
                         @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.")})
    @PatchMapping("/mypage")
    public ResponseEntity<MypageResponse> updateMyPage(
            @Valid @RequestBody MypageUpdateRequest request,
            @CurrentMemberId Long memberId
    ) {
        return ResponseEntity.ok(memberService.updateMyPage(memberId, request));
    }

    /**
     * 소비 유형별 소비 목표 반환
     * @param consumptionType 소비 유형
     * @return 소비 목표 list
     * @since 2025.05.07
     * @author 권민지
     */
    @Operation(summary = "소비 유형별 소비 목표 조회", description = "소비 유형에 따른 소비 목표를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "소비 목표 조회 결과 반환"),
                         @ApiResponse(responseCode = "400", description = "잘못된 소비 유형입니다.")})
    @GetMapping("/consume-goal/{consumptionType}")
    public ResponseEntity<ConsumeGoalResponse> getConsumeGoal(@PathVariable("consumptionType") ConsumptionType consumptionType) {
        return ResponseEntity.ok(memberService.getConsumeGoal(consumptionType));
    }

    /**
     * 사용자의 대출 status 조회
     * @return 대출 상태 (PRE_APPLIED, PENDING, REJECTED, EXECUTED, COMPLETED, NONE)
     * @since 2025.05.23
     * @author 권민지
     */
    @Operation(summary = "사용자의 대출 상태 조회", description = "사용자의 대출 상태를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "대출 상태 조회 결과 반환"),
                         @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.")})
    @GetMapping("/loan-status")
    public ResponseEntity<String> getLoanStatus(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(memberService.getLoanStatus(memberId));
    }

    /**
     * 사용자의 신용점수 평가 여부 조회
     * @return 신용점수 평가 여부
     * @since 2025.05.26
     * @author 유승한
     */
    @Operation(summary = "사용자의 신용점수 평가 여부 조회", description = "사용자의 신용점수 평가 여부를 조회합니다.",
            responses = {@ApiResponse(responseCode = "200", description = "사용자의 신용점수 평가 여부 반환"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.")})
    @GetMapping("/credit-score-status")
    public ResponseEntity<CreditScoreStatusResponse> getCreditScoreStatus(@CurrentMemberId Long memberId) {
        return ResponseEntity.ok(memberService.getCreditScoreStatus(memberId));
    }
}
