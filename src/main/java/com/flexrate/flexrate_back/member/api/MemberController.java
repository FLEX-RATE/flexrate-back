package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.dto.SignupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/*
 * @param 회원 서비스
 * @param signupDTO 회원 가입 요청 데이터
 * @return 없음 (성공 시 200 응답)
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

    @PostMapping("/signup")
    public void signup(@RequestBody SignupDTO signupDTO) {
        // 회원가입 로직 호출
        memberService.signup(signupDTO);
    }
}
