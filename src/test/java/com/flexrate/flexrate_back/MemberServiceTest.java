package com.flexrate.flexrate_back;

import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


    /*
    * DB 저장, 응답 DTO 확인
    * 중복 이메일 시 예외 발생 여부 테스트
    * @since 2025.05.02
    * @author 윤영찬
    * */

class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterMemberSuccessfully() {
        // Given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "user@naver.com",
                "password123",
                "MALE",
                "yeongchan",
                LocalDate.of(2000, 1, 1),
                "SAVING",
                "SAVING"
        );

        SignupResponseDTO expectedResponseDTO = new SignupResponseDTO(1L, "user@naver.com");

        when(memberRepository.existsByEmail(signupRequestDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(signupRequestDTO.password())).thenReturn("hashedPassword");

        when(memberRepository.save(any())).thenReturn(
                Member.builder()
                        .memberId(1L)
                        .email("user@naver.com")
                        .passwordHash("hashedPassword")
                        .name("yeongchan")
                        .sex(Sex.MALE)
                        .role(Role.MEMBER)
                        .birthDate(LocalDate.of(2000, 1, 1))
                        .status(MemberStatus.ACTIVE)
                        .consumptionType(ConsumptionType.SAVING)
                        .consumeGoal(ConsumeGoal.SAVING)
                        .build()
        );
        // When
        SignupResponseDTO result = memberService.registerMember(signupRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("user@naver.com", result.email());
    }

    @Test
    void testRegisterMemberWhenEmailAlreadyRegistered() {
        // Given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "user@naver.com",
                "password123",
                "Male",
                "yeongchan",
                LocalDate.of(2000, 1, 1),
                "SAVING",
                "SAVING"
        );

        when(memberRepository.existsByEmail(signupRequestDTO.email())).thenReturn(true);

        // When & Then
        assertThrows(FlexrateException.class, () -> memberService.registerMember(signupRequestDTO));
    }

}
