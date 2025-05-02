package com.flexrate.flexrate_back;

import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class SignupControllerTest {


    /*
    * 회원가입 성공 시, 요청 DTO에 대해 예상 응답 반환검증
    * @since 2025-05-02
    * @author 윤영찬
    * */
    @Test
    public void testSignupSuccessfully() {

        SignupRequestDTO requestDTO = new SignupRequestDTO(
                "user@naver.com",
                "password123",
                "Male",
                "User Name",
                LocalDate.of(1990, 1, 1),
                "SAVING",
                "SAVING"
        );

        SignupResponseDTO expectedResponseDTO = new SignupResponseDTO(
                1L,  // 예시 userId
                "user@naver.com"
        );

        MemberService memberService = mock(MemberService.class);
        when(memberService.registerMember(requestDTO)).thenReturn(expectedResponseDTO);
        SignupResponseDTO result = memberService.registerMember(requestDTO);

        assertNotNull(result, "회원가입 응답 null ");
        assertEquals(1L, result.userId(), "회원가입된 사용자 ID가 잘못되었습니다.");
        assertEquals("user@naver.com", result.email(), "회원가입된 이메일이 잘못되었습니다.");


        System.out.println("회원가입 성공!");
        System.out.println("등록된 사용자 ID: " + result.userId());
        System.out.println("등록된 이메일: " + result.email());
    }
}
