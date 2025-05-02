package com.flexrate.flexrate_back.auth.api;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.dto.CreateAccessTokenResponse;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import com.flexrate.flexrate_back.member.enums.Sex;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 테스트용 API
 * @author 유승한
 * @since 2025.05.01
 */
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class TestTokenApiController {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;

    @Operation(summary = "임의 토큰 발급", description = "로그인 기능이 없을 때 테스트용으로 JWT 발급하는 API입니다.")
    @PostMapping("/auth/generate-dev-token")
    public ResponseEntity<CreateAccessTokenResponse> generateDevToken(
            @RequestParam(defaultValue = "1") Long memberId,
            @RequestParam(defaultValue = "MEMBER") Role role) {

        Member fakeMember = Member.builder()
                .memberId(memberId)
                .email("test@dev.com")
                .passwordHash("test")
                .name("개발자")
                .phone("010-0000-0000")
                .sex(Sex.MALE)
                .role(role)
                .status(MemberStatus.ACTIVE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String token = jwtTokenProvider.generateToken(fakeMember, Duration.ofHours(1));

        return ResponseEntity.ok(new CreateAccessTokenResponse(token));
    }

    @GetMapping("/auth/test-token")
    @Operation(
            summary = "현재 사용자 정보 조회",
            description = "테스트용으로 요청 헤더에 포함된 JWT 토큰을 기반으로 사용자 인증 정보를 반환합니다.",
            security = { @SecurityRequirement(name = "Bearer Authentication") }
    )
    public ResponseEntity<String> getCurrentUser(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token != null && jwtTokenProvider.validToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            String userInfo = String.format("인증된 사용자: %s, 권한: %s",
                    auth.getName(),
                    auth.getAuthorities().toString()
            );
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(401).body("유효하지 않은 토큰입니다.");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Operation(
            summary = "현재 사용자 상세 정보 조회",
            description = "JWT 토큰을 기반으로 Principal에서 사용자 ID를 추출하여 Member 정보를 조회합니다.",
            security = { @SecurityRequirement(name = "Bearer Authentication") },
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원 정보 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": \"A004\", \"message\": \"유효하지 않은 리프레시 토큰입니다.\"}")
                    )),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": \"S500\", \"message\": \"서버 내부 오류\"}")
                    ))
            }
    )
    @GetMapping("/test-principal")
    public ResponseEntity<Member> getMyMemberInfo(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        Member member = getMember(principal);
        return ResponseEntity.ok(member);
    }

    // 본래 getMember이지만 테스트 api 상 principal의 id 가 1일 시 fakeMember 반환
    private Member getMember(Principal principal) {
        if(!principal.getName().equals("1")) throw new FlexrateException(ErrorCode.USER_NOT_FOUND);
        Member fakeMember = Member.builder()
                .memberId(1l)
                .email("test@dev.com")
                .passwordHash("test")
                .name("개발자")
                .phone("010-0000-0000")
                .sex(Sex.MALE)
                .role(Role.MEMBER)
                .status(MemberStatus.ACTIVE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
//        return memberService.findById(Long.parseLong(principal.getName()));
        return fakeMember;
    }

}
