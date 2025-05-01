package com.flexrate.flexrate_back.auth.application;

import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
import com.flexrate.flexrate_back.auth.domain.jwt.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 관리자 권한으로 회원 목록 조회
     *
     * @param refreshToken 클라이언트로부터 전달받은 리프레시 토큰
     * @return RefreshToken 토큰 정보 객체
     * @throws IllegalArgumentException 유효하지 않은 리프레시 토큰일 경우
     * @since 2025.05.01
     * @author 유승한
     */
    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Unexpected token"));
    }
}
