package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.auth.domain.FidoCredential;
import com.flexrate.flexrate_back.member.domain.repository.FidoCredentialRepository;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.LoginRequestDTO;
import com.flexrate.flexrate_back.member.dto.LoginResponseDTO;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.member.dto.PasskeyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


/*
 * 이메일과 비밀번호로 로그인 인증
 * @since 2025.05.06
 * @author 윤영찬
 * @param loginRequestDTO 로그인 요청 데이터
 * @return 로그인 성공 시 사용자 정보 및 패스키 인증 여부
 */

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;
    private final FidoCredentialRepository fidoCredentialRepository;

    public LoginResponseDTO loginWithEmailAndPassword(LoginRequestDTO loginRequestDTO) {
        Member member = memberRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_CREDENTIALS));

        if (!member.getPasswordHash().equals(loginRequestDTO.password())) {
            throw new FlexrateException(ErrorCode.INVALID_CREDENTIALS);
        }

        boolean requirePasskeyAuth = isPasskeyRegistered(member.getMemberId());

        if (requirePasskeyAuth) {
            throw new FlexrateException(ErrorCode.AUTHENTICATION_REQUIRED);
        }

        List<PasskeyDTO> passkeyDTOs = fidoCredentialRepository.findByMember_MemberId(member.getMemberId())
                .stream()
                .map(c -> PasskeyDTO.builder()
                        .build())
                .toList();

        return LoginResponseDTO.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .requirePasskeyAuth(requirePasskeyAuth)
                .build();
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new FlexrateException(ErrorCode.INVALID_EMAIL_FORMAT));
    }

    private boolean isPasskeyRegistered(Long memberId) {
        return fidoCredentialRepository.findByMember_MemberId(memberId)
                .map(FidoCredential::isActive)
                .orElse(false);
    }
}
