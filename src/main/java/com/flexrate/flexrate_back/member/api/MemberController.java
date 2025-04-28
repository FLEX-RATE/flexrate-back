package com.flexrate.flexrate_back.member.api;

import com.flexrate.flexrate_back.member.dto.SignupDTO;
import com.flexrate.flexrate_back.member.application.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
