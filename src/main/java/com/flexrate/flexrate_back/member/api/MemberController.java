package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
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
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /*
     * 회원가입
     * @param signupDTO 데이터
     * @return 생성된 회원 ID, 이메일 (201 Created)
     * @throws FlexrateException 유효성 검사 또는 중복 등 오류 발생 시
     * @since 2025.04.28
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(
            @RequestBody @Valid SignupDTO signupDTO) {
        Member created = memberService.registerMember(signupDTO);
        SignupResponseDTO body = SignupResponseDTO.builder()
                .userId(created.getMemberId())
                .email(created.getEmail())
                .build();
        return ResponseEntity
                .status(201)
                .body(body);
    }

    /*
     * 로그인 (테스트용)
     * @param email    로그인 이메일
     * @param password 로그인 비밀번호
     * @return 로그인 성공/실패 메시지 (200 OK)
     * @since 2025.04.28
     * @author 윤영찬
     */
//    @PostMapping("/login")
//    public ResponseEntity<String> login(
//            @RequestParam String email,
//            @RequestParam String password) {
//        boolean ok = memberService.authenticate(email, password);
//        return ResponseEntity.ok(ok ? "로그인 성공" : "로그인 실패");
//    }


    /*
     * 비밀번호 변경
     * @param dto 비밀번호 변경 요청 데이터
     * @return 성공 메시지
     * @since 2025.04.29
     * @author 윤영찬
     */
//    @PutMapping("/password")
//    public ResponseEntity<String> changePassword(@RequestBody @Valid PasswordChangeDTO dto) {
//        memberService.changePassword(dto);
//        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
//    }
}
