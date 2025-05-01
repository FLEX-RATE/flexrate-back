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
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    public String createNewAccessToken(String refreshToken) {
        // 토큰 유효성 검사 실패 시 예외 발생
        if(!tokenProvider.validToken(refreshToken)){
            throw new IllegalArgumentException("Unexpected token");
        }
        Long memberId = refreshTokenService.findByRefreshToken(refreshToken).getMemberId();
        Member member = memberService.findById(memberId);

        return tokenProvider.generateToken(member, Duration.ofHours(2));
    }
}
