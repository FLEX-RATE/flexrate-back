package com.flexrate.flexrate_back.loan.api;

import com.flexrate.flexrate_back.loan.application.LoanProductService;
import com.flexrate.flexrate_back.loan.dto.LoanProductSummaryDto;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanProductController {
    private final LoanProductService loanProductService;
    private final MemberService memberService;
    /**
     * 등록된 모든 대출 상품 목록을 조회하는 API
     *
     * @return 대출 상품 정보 리스트
     * @since 2025.05.05
     * @author 유승한
     */
    @Operation(
            summary = "대출 상품 목록 조회",
            description = "등록된 모든 대출 상품 정보를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대출 상품 목록 조회 성공")
            }
    )
    @GetMapping
    public List<LoanProductSummaryDto> getAllProducts() {
        return loanProductService.getAllProducts();
    }

    /**
     * 선택한 대출 상품을 기반으로 빈 LoanApplication을 생성하는 API
     *
     * 기존 신청이 'PRE-REVIEW' 상태일 경우 삭제 후 새로 생성되며,
     * 다른 상태의 신청이 존재하면 예외가 발생합니다.
     *
     * @param productId 선택한 대출 상품 ID
     * @param principal 인증된 사용자 Principal
     * @return HTTP 200 OK (성공 시)
     * @throws FlexrateException 중복 대출 신청 시 예외 발생 (DUPLICATE_LOAN_APPLICATION)
     * @since 2025.05.05
     */
    @Operation(
            summary = "대출 상품 선택",
            description = "사용자가 대출 상품을 선택하면 해당 상품을 기반으로 빈 LoanApplication을 생성합니다.",
            parameters = {
                    @Parameter(name = "productId", description = "선택한 대출 상품 ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "LoanApplication 생성 성공"),
            }
    )
    @PostMapping("/{productId}/select")
    public ResponseEntity<Void> selectProduct(
            @PathVariable Long productId,
            Principal principal
    ) {
        Member member = getMember(principal);
        loanProductService.selectProduct(productId, member);
        return ResponseEntity.ok().build();
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
