package com.flexrate.flexrate_back.jwt;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtProperties;
import com.flexrate.flexrate_back.auth.domain.jwt.JwtTokenProvider;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Role;
import com.flexrate.flexrate_back.member.enums.Sex;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class TokenProviderTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProperties jwtProperties;

    @DisplayName("JWT SECRET KEY(): 제대로 시크릿키를 인식한다.")
    @Test
    void test(){
        System.out.println("JWT Secret Key >>> " + jwtProperties.getSecretKey());
    }

    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
    @Test
    void generateToken() {
        // given
        Member testUser = memberRepository.save(Member.builder()
                .email("user@gmail.com")
                .passwordHash("test")
                .name("테스트")
                .phone("010-1234-5678")
                .sex(Sex.MALE)
                .role(Role.MEMBER)
                .status(MemberStatus.ACTIVE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // when
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // then
        Long memberId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);

        assertThat(memberId).isEqualTo(testUser.getMemberId());
    }

    @DisplayName("validToken(): 만료된 토큰인 경우에 유효성 검증에 실패한다.")
    @Test
    void validToken_invalidToken() {
        // given
        String token = JwtFactory.builder()
                .expiration(new Date(System.currentTimeMillis() - Duration.ofDays(1).toMillis()))
                .build()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("validToken(): 유효한 토큰인 경우에 유효성 검증에 성공한다.")
    @Test
    void validToken_validToken() {
        // given
        String token = JwtFactory.withDefaultValues()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("getAuthentication(): 토큰 기반으로 인증정보를 가져올 수 있다.")
    @Test
    void getAuthentication() {
        // given
        String userId = "1"; // subject가 memberId로 들어가므로
        String token = JwtFactory.builder()
                .subject(userId)
                .claims(Map.of("id", 1L, "role", "USER"))
                .build()
                .createToken(jwtProperties);

        // when
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userId);
    }

    @DisplayName("getMemberId(): 토큰으로 멤버 ID를 가져올 수 있다.")
    @Test
    void getUserId() {
        // given
        Long memberId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", memberId, "role", "USER"))
                .build()
                .createToken(jwtProperties);

        // when
        Long extractedId = tokenProvider.getMemberId(token);

        // then
        assertThat(extractedId).isEqualTo(memberId);
    }
}
