package com.flexrate.flexrate_back.financialdata.api;

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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/credit-score")
@RequiredArgsConstructor
public class CreditScoreController {

    private final UserFinancialDataService userFinancialDataService;
    private final MemberService memberService;

    /**
     * 회원의 신용점수 조회 api
     * @param principal 인증된 사용자 정보
     * @return 계산된 신용점수 (0~1000 범위)
     */
    @Operation(
            summary = "신용점수 조회",
            description = "회원의 신용점수를 반환합니다."
    )
    @GetMapping
    public ResponseEntity<Integer> getCreditScore(Principal principal) {
        Member member = memberService.findById(Long.parseLong(principal.getName()));
        int creditScore = userFinancialDataService.evaluateCreditScore(member);
        return ResponseEntity.ok(creditScore);
    }

    /**
     * 회원의 금융 데이터를 바탕으로 신용점수를 평가하는 API
     * @param principal 인증된 사용자 정보
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
    public ResponseEntity<CreditScoreResponse> evaluateCreditScore(Principal principal) {
        Member member = memberService.findById(Long.parseLong(principal.getName()));
        int creditScore = userFinancialDataService.evaluateCreditScore(member);
        return ResponseEntity.ok(new CreditScoreResponse(creditScore));
    }

}
