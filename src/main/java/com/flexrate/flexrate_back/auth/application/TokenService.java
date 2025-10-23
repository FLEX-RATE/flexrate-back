package com.flexrate.flexrate_back.auth.application;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.util.StringRedisUtil;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.flexrate.flexrate_back.member.domain.QMember.member;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final JwtTokenProvider tokenProvider;
    private final MemberService memberService;
    private final StringRedisUtil redisUtil;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    /**
     * 리프레쉬 토큰으로 액세스 토큰 재발급
     *
     * @param refreshToken 클라이언트로부터 전달받은 리프레시 토큰
     * @return 새로 발급된 액세스 토큰 문자열
     * @throws IllegalArgumentException 유효하지 않은 토큰일 경우
     * @since 2025.05.01
     * @author 유승한
     */
    public String createNewAccessToken(String refreshToken, HttpServletResponse response) {
        // 1. JWT 유효성 검사
        if (!tokenProvider.validToken(refreshToken)) {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. Redis에서 토큰 값으로 사용자 ID 조회
        String redisKey = "refreshToken:" + refreshToken;
        String memberIdStr = redisUtil.get(redisKey);

        if (memberIdStr == null) {
            throw new FlexrateException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. Member 엔티티 조회
        Long memberId = Long.parseLong(memberIdStr);
        Member member = memberService.findById(memberId);

        // 4. 새로운 accessToken 생성
        String newAccessToken = tokenProvider.generateAccessToken(member, Duration.ofHours(2));

        ResponseCookie accessCookie = ResponseCookie.from("access_token", newAccessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite("Strict")
                .maxAge(2 * 60 * 60)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return newAccessToken;
    }
}