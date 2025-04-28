package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.loan.application.LoanPreApplicationService;
import com.flexrate.flexrate_back.loan.dto.LoanPreApplicationRequest;
import com.flexrate.flexrate_back.loan.dto.LoanPreApplicationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanPreApplicationController {

    private final LoanPreApplicationService preLoanApplicationService;

    /**
     * 대출 신청 사전 정보를 입력 받아 심사 결과를 반환하는 API
     * @param request 대출 신청 사전 정보
     * TODO: 멤버 토큰 및 예외 처리 관련 코드 추가
     * @return 대출 심사 결과
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
                    @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"code\": \"V001\", \"message\": \"필수 입력값이 누락되었습니다.\"}"))),
                    @ApiResponse(responseCode = "401", description = "인증 실패",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(value = "{\"code\": \"A006\", \"message\": \"인증이 필요합니다.\"}")))
            }
    )
    @PostMapping("/pre-applications")
    public LoanPreApplicationResponse preApplyLoan(
            @RequestBody @Valid LoanPreApplicationRequest request
    ) {
        return preLoanApplicationService.preApply(request);
    }
}
