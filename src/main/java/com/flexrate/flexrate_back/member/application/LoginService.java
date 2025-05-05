package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.LoginMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request.email() == null || request.email().isBlank()) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        if (request.passkeyId() != null && !request.passkeyId().isBlank()) {
            // 패스키 로그인 (추후 구현 예정)
            throw new FlexrateException(ErrorCode.PASSKEY_AUTH_FAILED);
        }

        if (request.password() == null || request.password().isBlank()) {
            throw new FlexrateException(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
        }

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

//        member.setLastLoginAt(LocalDateTime.now());
//        member.setLastLoginMethod(LoginMethod.PASSWORD);
        memberRepository.save(member); // 변경된 값 저장

//        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
//        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        List<String> passkeyList = Collections.emptyList();

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                member.getName(),
                passkeyList
        );
    }
}
