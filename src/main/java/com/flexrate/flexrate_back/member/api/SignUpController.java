package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.SignupMemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * 회원가입 로그인 API 컨트롤러
 * @since 2025.04.28
 * @author 윤영찬
 */
@RestController
@RequestMapping("/members")
public class SignUpController {

    private final SignupMemberService signupMemberService;


    @Autowired
    public SignUpController(SignupMemberService signupMemberService) {
        this.signupMemberService = signupMemberService;
    }

    /*
     * 회원가입
     * @param signupDTO 데이터
     * @return 생성된 회원 ID, 이메일 (201 Created)
     * @throws FlexrateException 유효성 검사 또는 중복 등 오류 발생 시
     * @since 2025.04.28
     */
    @Operation(
            summary = "회원가입",
            description = "사용자로부터 이메일, 비밀번호, 이름 등의 정보를 입력받아 회원을 등록합니다. " +
                    "이메일 중복 여부와 입력값의 유효성을 검사합니다.",
            tags = { "Sign-up-controller" }
    )
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(
            @RequestBody @Valid SignupDTO signupDTO) {
        Member created = signupMemberService.registerMember(signupDTO);
        SignupResponseDTO body = SignupResponseDTO.builder()
                .userId(created.getMemberId())
                .email(created.getEmail())
                .build();
        return ResponseEntity
                .status(201)
                .body(body);
    }
}
