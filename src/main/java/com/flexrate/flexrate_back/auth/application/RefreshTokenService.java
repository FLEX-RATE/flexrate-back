package com.flexrate.flexrate_back.auth.application;

import com.flexrate.flexrate_back.auth.domain.repository.RefreshTokenRepository;
import com.flexrate.flexrate_back.auth.domain.jwt.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken findByRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()-> new IllegalArgumentException("Unexpected token"));
    }
}
