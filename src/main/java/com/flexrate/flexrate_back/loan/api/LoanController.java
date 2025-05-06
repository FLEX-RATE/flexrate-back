package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.loan.application.LoanService;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final MemberService memberService;

    /**
     * 대출 신청 사전 정보를 입력 받아 심사 결과를 반환하는 API
     * @param request 대출 신청 사전 정보
     * TODO: 멤버 토큰 및 예외 처리 관련 코드 추가
     * @since 2025.04.28
     * @author 서채연
     */
    @Operation(
            summary = "대출 신청 사전 정보 입력",
            description = "고객의 기본 대출 신청 정보를 입력받아 대출 심사 결과를 반환합니다.",
            parameters = {
                    @Parameter(name = "request", description = "대출 신청 사전 정보", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "대출 심사 결과 반환"),
            }
    )
    @PostMapping("/loan-review-application")
    public ResponseEntity<Void> preApplyLoan(
            @RequestBody @Valid LoanReviewApplicationRequest request,
            Principal principal
    ) {
        Member member = getMember(principal);
        loanService.preApply(request, member);
        return ResponseEntity.ok().build();

    }

    /**
     * 사전 신청 결과 조회 API
     * @return 대출 사전 심사 결과
     * @since 2025.05.06
     * @author 유승한
     */
    @Operation(
            summary = "사전 신청 결과 조회",
            description = "사전 신청된 대출의 외부 심사 결과를 조회합니다."
    )
    @GetMapping("/loan-review-application")
    public ResponseEntity<LoanReviewApplicationResponse> preApplyResult(
            Principal principal
    ) {
        LoanReviewApplicationResponse result = loanService.preApplyResult(getMember(principal));
        return ResponseEntity.ok(result);
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
