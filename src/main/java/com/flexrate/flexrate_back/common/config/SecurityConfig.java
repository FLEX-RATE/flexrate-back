package com.flexrate.flexrate_back.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            )
            .sessionManagement(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        // JWT 인증 필터 추가 필요
        return http.build();
    }



    /*
     * 비밀번호를 암호화하기 위한 빈 등록
     * @return BCryptPasswordEncoder 인스턴스
     * @since 2025.04.28
     * @author 윤영찬
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
