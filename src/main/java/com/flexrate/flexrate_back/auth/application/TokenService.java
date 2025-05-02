package com.flexrate.flexrate_back.auth.application;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final JwtTokenProvider tokenProvider;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 리프레쉬 토큰으로 액세스 토큰 재발급
     *
     * @param refreshToken 클라이언트로부터 전달받은 리프레시 토큰
     * @return 새로 발급된 액세스 토큰 문자열
     * @throws IllegalArgumentException 유효하지 않은 토큰일 경우
     * @since 2025.05.01
     * @author 유승한
     */
    public String createNewAccessToken(String refreshToken) {
        // 토큰 유효성 검사 실패 시 예외 발생
        if (!tokenProvider.validToken(refreshToken)) {
            throw new IllegalArgumentException("Unexpected token");
        }
        Long memberId = refreshTokenService.findByRefreshToken(refreshToken).getMemberId();
        Member member = memberService.findById(memberId);

        return tokenProvider.generateToken(member, Duration.ofHours(2));
    }
}
