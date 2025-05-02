package com.flexrate.flexrate_back;

import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.MemberService;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.dto.SignupResponseDTO;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
                "MALE", // Sex enum을 사용할 때는 "MALE"로 사용해야 합니다.
                "User Name",
                LocalDate.of(1990, 1, 1),
                "SAVING",
                "SAVING"
        );

        SignupResponseDTO expectedResponseDTO = new SignupResponseDTO(1L, "user@naver.com");

        // Mock behavior
        when(memberRepository.existsByEmail(signupRequestDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(signupRequestDTO.password())).thenReturn("hashedPassword");

        // Simulate member saving
//        when(memberRepository.save(any())).thenReturn(new Member(1L, "user@naver.com", "hashedPassword", "User Name", Sex.MALE, LocalDate.of(1990, 1, 1), MemberStatus.ACTIVE, ConsumptionType.SAVING, ConsumeGoal.SAVING));

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
                "User Name",
                LocalDate.of(1990, 1, 1),
                "SAVING",
                "SAVING"
        );

        // Mock behavior
        when(memberRepository.existsByEmail(signupRequestDTO.email())).thenReturn(true);

        // When & Then
        assertThrows(FlexrateException.class, () -> memberService.registerMember(signupRequestDTO));
    }

    @Test
    void testRegisterMemberWithInvalidConsumptionType() {
        // Given
        SignupRequestDTO signupRequestDTO = new SignupRequestDTO(
                "user@naver.com",
                "password123",
                "Male",
                "User Name",
                LocalDate.of(1990, 1, 1),
                "INVALID", // 잘못된 소비성향
                "SAVING"
        );

        // When & Then
        assertThrows(FlexrateException.class, () -> memberService.registerMember(signupRequestDTO));
    }
}
