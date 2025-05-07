package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.dto.MypageResponse;
import com.flexrate.flexrate_back.member.dto.MypageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

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
    public ResponseEntity<MypageResponse> getMyPage(Principal principal) {
        Long memberId = Long.parseLong(principal.getName());
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
    public ResponseEntity<MypageResponse> updateMyPage(MypageUpdateRequest request, Principal principal) {
        Long memberId = Long.parseLong(principal.getName());
        return ResponseEntity.ok(memberService.updateMyPage(memberId, request));
    }
}
