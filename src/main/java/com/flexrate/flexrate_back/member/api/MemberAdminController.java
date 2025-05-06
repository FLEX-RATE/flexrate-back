package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberAdminService;
import com.flexrate.flexrate_back.member.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    /**
     * 관리자 권한으로 회원 정보 수정
     * @param request 회원 정보 수정 요청
     * @param adminToken 관리자 인증 토큰
     * @return 회원 정보 수정 결과
     * @since 2025.04.26
     * @author 허연규
     */
    @Operation(summary = "관리자 권한으로 회원 정보 수정", description = "관리자가 회원 id를 통해 특정 회원의 정보를 수정합니다.",
    parameters = {
            @Parameter(name = "memberId", description = "수정할 memberId", required = true),
            @Parameter(name = "X-Admin-Token", description = "관리자 인증 토큰", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 정보 수정 결과 반환"),
                    @ApiResponse(responseCode = "400", description = "관리자 인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"A007\", \"message\": \"관리자 권한이 필요합니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "사용자가 존재하지 않음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"U001\", \"message\": \"사용자를 찾을 수 없습니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "필수 입력값 누락", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"A000\", \"message\": \"필수 입력값이 누락되었습니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "유효성 검사 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"V001\", \"message\": \"유효성 검사 오류\"}"))),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"S500\", \"message\": \"서버 내부 오류\"}"))),
            })
    @PatchMapping("/{memberId}")
    public ResponseEntity<PatchMemberResponse> patchMember(
            @PathVariable Long memberId,
            @Valid @RequestBody PatchMemberRequest request,
            @RequestHeader("X-Admin-Token") String adminToken
    ) {
        return ResponseEntity.ok(
                memberAdminService.patchMember(memberId, request, adminToken)
        );
    }

    /**
     * 관리자 권한으로 회원 정보 상세 조회
     * @param memberId 조회할 회원 Id
     * @param adminToken 관리자 인증 토큰
     * @return 회원 상세 정보 조회 결과
     * @since 2025.04.29
     * @author 허연규
     */

    @Operation(summary = "관리자 권한으로 회원 정보 상세 조회", description = "관리자가 회원 id를 통해 특정 회원의 정보를 상세 조회합니다.",
            parameters = {
                    @Parameter(name = "memberId", description = "조회할 memberId", required = true),
                    @Parameter(name = "X-Admin-Token", description = "관리자 인증 토큰", required = true)},
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 정보 조회 결과 반환"),
                    @ApiResponse(responseCode = "400", description = "관리자 인증 실패", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"A007\", \"message\": \"관리자 권한이 필요합니다.\"}"))),
                    @ApiResponse(responseCode = "400", description = "사용자가 존재하지 않음", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"U001\", \"message\": \"사용자를 찾을 수 없습니다.\"}"))),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"code\": \"S500\", \"message\": \"서버 내부 오류\"}"))),
            }
            //security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/{memberId}")
    // @PreAuthorize("hasRole('ADMIN')") - 우선은 adminToken으로 테스트
    public ResponseEntity<MemberDetailResponse> getMemberDetail(
            @PathVariable Long memberId,
            @RequestHeader("X-Admin-Token") String adminToken
    ) {
        return ResponseEntity.ok(
                memberAdminService.searchMemberDetail(memberId, adminToken)
        );
    }
}
