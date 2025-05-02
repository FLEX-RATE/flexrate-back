package com.flexrate.flexrate_back.auth.api;

import com.flexrate.flexrate_back.auth.application.TokenService;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.dto.CreateAccessTokenResponse;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import com.flexrate.flexrate_back.member.enums.Sex;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 액세스 토큰 발급 API
 * @author 유승한
 * @since 2025.05.01
 */
@RequiredArgsConstructor
@RestController("/api/auth")
public class TokenApiController {
    private final TokenService tokenService;

    /**
     * 리프레시 토큰을 통해 새로운 액세스 토큰을 발급합니다.
     *
     * @return 생성된 액세스 토큰
     * @throws IllegalArgumentException 유효하지 않은 토큰일 경우 예외 발생
     */
    @Operation(
            summary = "액세스 토큰 재발급",
            description = "쿠키에 저장된 리프레시 토큰으로 액세스 토큰을 재발급하는 API <br>파라미터 필요없이 execute 하면 됩니다",
            responses = {
                    @ApiResponse(responseCode = "201", description = "액세스 토큰 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰", content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": \"A001\", \"message\": \"유효하지 않은 토큰입니다.\"}")
                    )),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"code\": \"S500\", \"message\": \"서버 내부 오류\"}")
                    ))
            }
    )
    @PostMapping("/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@CookieValue(value = "refresh_token", required = false) String refreshToken){
        if (refreshToken == null) {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = tokenService.createNewAccessToken(refreshToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
    }

    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "임의 토큰 발급", description = "로그인 기능이 없을 때 테스트용으로 JWT 발급하는 API입니다.")
    @PostMapping("/generate-dev-token")
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

    @GetMapping("/test-token")
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
            return ResponseEntity.status(401).body("❌ 유효하지 않은 토큰입니다.");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}
