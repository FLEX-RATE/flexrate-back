package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberAdminService;
import com.flexrate.flexrate_back.member.dto.MemberSearchRequest;
import com.flexrate.flexrate_back.member.dto.MemberSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
public class MemberAdminController {
    private final MemberAdminService memberAdminService;

    /**
     * 관리자 권한으로 회원 목록 조회
     * @param request 회원 검색 요청
     * @param adminToken 관리자 인증 토큰
     * @return 회원 검색 결과
     * @since 2025.04.26
     * @author 권민지
     */
    @Operation(summary = "관리자 권한으로 회원 목록 조회", description = "관리자가 회원 목록을 검색 조건에 따라 조회합니다.",
            parameters = {@Parameter(name = "request", description = "회원 검색 요청(모든 인자 null 가능)", required = true),
                          @Parameter(name = "X-Admin-Token", description = "관리자 인증 토큰", required = true)},
            responses = {@ApiResponse(responseCode = "200", description = "회원 검색 결과 반환"),
                         @ApiResponse(responseCode = "400", description = "관리자 인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"A007\", \"message\": \"관리자 권한이 필요합니다.\"}"))),})
    @GetMapping("/search")
    public ResponseEntity<MemberSearchResponse> searchMembers(
            @Valid MemberSearchRequest request,
            @RequestHeader("X-Admin-Token") String adminToken
    ) {
        return ResponseEntity.ok(memberAdminService.searchMembers(request, adminToken));
    }
}
