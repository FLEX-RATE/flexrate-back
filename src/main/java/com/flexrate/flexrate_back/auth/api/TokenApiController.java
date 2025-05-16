package com.flexrate.flexrate_back.auth.api;

import com.flexrate.flexrate_back.auth.application.TokenService;
import com.flexrate.flexrate_back.auth.dto.CreateAccessTokenResponse;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 액세스 토큰 발급 API
 * @author 유승한
 * @since 2025.05.01
 */
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
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
}
