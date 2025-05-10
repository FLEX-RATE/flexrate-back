/*
package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    @Value("${social.kakao.api.url}")
    private String kakaoApiUrl; // 카카오 API URL (예: https://kapi.kakao.com/v2/user/me)

    @Value("${social.kakao.api.key}")
    private String kakaoApiKey; // 카카오 API 키 (예: 'kakao_rest_api_key')

    public String loginWithKakao(String accessToken) {
        // 카카오 API 호출하여 사용자 정보 가져오기
        String url = kakaoApiUrl;
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, accessToken);

        if (response.getStatusCode().is2xxSuccessful()) {
            String kakaoUserInfo = response.getBody();

            // 사용자 정보에서 email, name, id 등 필요한 정보 추출
            String email = extractEmail(kakaoUserInfo);
            String name = extractName(kakaoUserInfo);

            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateToken(member, Duration.ofHours(2));

            return accessToken; // JWT 토큰 반환
        } else {
            throw new FlexrateException(ErrorCode.SOCIAL_AUTH_FAILED);
        }
    }

    private String extractEmail(String kakaoUserInfo) {
        return "user@example.com"; // 이메일
    }

    private String extractName(String kakaoUserInfo) {
        return "Kakao User"; // 이름
    }
}
*/
