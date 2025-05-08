package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.loan.application.InterestService;
import com.flexrate.flexrate_back.loan.dto.InterestAnalysisRequest;
import com.flexrate.flexrate_back.loan.dto.InterestResponse;
import com.flexrate.flexrate_back.loan.dto.InterestSummaryResponse;
import com.flexrate.flexrate_back.loan.enums.PeriodType;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;
    private final MemberService memberService;

    /**
     * 현재 대출 금리 및 변동률 조회
     */
    @Operation(
            summary = "현재 이자율 및 변동률 조회",
            description = "최근 두 개의 이자율을 비교하여 현재 이자율과 변동 퍼센트를 제공합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "404", description = "이자 정보 없음", content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": \"L008\", \"message\": \"이자 정보가 존재하지 않습니다.\"}")
                    ))
            }
    )
    @GetMapping("/interest/current")
    public ResponseEntity<InterestResponse> getCurrentInterestChange(
            Principal principal
    ) {
        Member member = getMember(principal);
        InterestResponse response = interestService.getCurrentInterestChange(member.getLoanApplication().getApplicationId());
        return ResponseEntity.ok(response);
    }

    /**
     * 기간별 이자율 통계 조회
     */
    @Operation(
            summary = "기간별 이자율 통계 조회",
            description = "일별, 주별, 월별 평균 이자율과 상승률, 최고/최저 이자율 정보를 제공합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "통계 조회 성공"),
                    @ApiResponse(responseCode = "404", description = "이자 정보 없음", content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": \"L008\", \"message\": \"이자 정보가 존재하지 않습니다.\"}")
                    ))
            }
    )
    @GetMapping("/interest/stats")
    public ResponseEntity<InterestSummaryResponse> getInterestStats(
            @RequestParam PeriodType periodType,
            Principal principal
    ) {
        Member member = getMember(principal);
        InterestSummaryResponse response = interestService.getInterestStats(
                new InterestAnalysisRequest(member.getLoanApplication().getApplicationId(), periodType)
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Principal 객체를 통해 현재 로그인한 회원을 조회합니다.
     *
     * @param principal Spring Security에서 주입한 사용자 인증 정보
     * @return 해당 사용자의 Member 엔티티
     */
    private Member getMember(Principal principal) {
        return memberService.findById(Long.parseLong(principal.getName()));
    }
}
