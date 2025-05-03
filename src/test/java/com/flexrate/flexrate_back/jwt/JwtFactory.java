package com.flexrate.flexrate_back.jwt;

import com.flexrate.flexrate_back.auth.domain.jwt.JwtProperties;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
public class JwtFactory {

    private String subject = "1"; // memberId를 subject로 사용
    private Date issuedAt = new Date();
    private Date expiration = new Date(new Date().getTime() + Duration.ofDays(14).toMillis());
    private Map<String, Object> claims = new HashMap<>();

    @Builder
    public JwtFactory(String subject, Date issuedAt, Date expiration, Map<String, Object> claims) {
        if (subject != null) this.subject = subject;
        if (issuedAt != null) this.issuedAt = issuedAt;
        if (expiration != null) this.expiration = expiration;
        if (claims != null) this.claims = claims;
    }

    public static JwtFactory withDefaultValues() {
        Map<String, Object> defaultClaims = new HashMap<>();
        defaultClaims.put("id", 1L);
        defaultClaims.put("role", "USER");

        return JwtFactory.builder()
                .subject("1")
                .claims(defaultClaims)
                .build();
    }

    public String createToken(JwtProperties jwtProperties) {
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }
}
