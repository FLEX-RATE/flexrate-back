package com.flexrate.flexrate_back.financialdata.api;

import com.flexrate.flexrate_back.auth.resolver.CurrentMemberId;
import com.flexrate.flexrate_back.financialdata.application.UserFinancialDataService;
import com.flexrate.flexrate_back.financialdata.dto.CreditScoreResponse;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/credit-score")
@RequiredArgsConstructor
public class CreditScoreController {

    private final UserFinancialDataService userFinancialDataService;
    private final MemberService memberService;

    /**
     * 회원의 신용점수 조회 api
     * @param memberId 현재 로그인한 회원의 ID
     * @return 계산된 신용점수 (0~1000 범위)
     */
    @Operation(
            summary = "신용점수 조회",
            description = "회원의 신용점수를 반환합니다."
    )
    @GetMapping
    public ResponseEntity<CreditScoreResponse> getCreditScore(@CurrentMemberId Long memberId) {
        Member member = memberService.findById(memberId);
        int creditScore = member.getLoanApplication().getCreditScore();
        int percentile = userFinancialDataService.getCreditScorePercentile(creditScore);
        return ResponseEntity.ok(new CreditScoreResponse(creditScore, percentile));
    }

    /**
     * 회원의 금융 데이터를 바탕으로 신용점수를 평가하는 API
     * @param memberId 현재 로그인한 회원의 ID
     * @return 계산된 신용점수 (0~1000 범위)
     */
    @Operation(
            summary = "신용점수 계산",
            description = "회원의 금융 데이터를 바탕으로 신용점수를 계산하여 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 계산된 신용점수 반환",
                            content = @Content(schema = @Schema(implementation = CreditScoreResponse.class)))
            }
    )
    @GetMapping("/evaluate")
    public ResponseEntity<CreditScoreResponse> evaluateCreditScore(@CurrentMemberId Long memberId) {
        Member member = memberService.findById(memberId);
        int creditScore = userFinancialDataService.evaluateCreditScore(member);
        int percentile = userFinancialDataService.getCreditScorePercentile(creditScore);
        return ResponseEntity.ok(new CreditScoreResponse(creditScore, percentile));
    }

}
