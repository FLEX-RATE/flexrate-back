package com.flexrate.flexrate_back;

import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.application.SignupMemberService;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.SignupRequestDTO;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.enums.ConsumptionType;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SignupMemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupMemberService signupMemberService;

    private SignupRequestDTO signupRequestDTO;

    @BeforeEach
    void setUp() {
        signupRequestDTO = new SignupRequestDTO(
                "user@gmail.com",
                "password123!",
                "MALE",
                "yoon",
                LocalDate.of(1990, 1, 1),
                ConsumptionType.CONSERVATIVE,
                "졀약형"
        );
    }

    @Test
    void testRegisterMember() {
        // Given
        Member member = Member.builder()
                .memberId(1L)
                .email("user@gmail.com")
                .passwordHash("encodedPassword")
                .name("yoon")
                .sex(Sex.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.existsByEmail(signupRequestDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member createdMember = signupMemberService.registerMember(signupRequestDTO);

        // Then
        assertNotNull(createdMember);
        assertEquals("user@gmail.com", createdMember.getEmail());
        verify(memberRepository).save(any(Member.class));  // memberRepository.save()가 호출되었는지 확인
        verify(passwordEncoder).encode("password123!");  // 패스워드가 인코딩 되었는지 확인
    }

    @Test
    void testEmailAlreadyExists() {
        // Given
        when(memberRepository.existsByEmail(signupRequestDTO.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(FlexrateException.class, () -> {
            signupMemberService.registerMember(signupRequestDTO);
        }, "이미 존재하는 이메일입니다.");
    }

    @Test
    void testPasswordEncoding() {
        // Given
        when(memberRepository.existsByEmail(signupRequestDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password123!")).thenReturn("encodedPassword");

        // When
        signupMemberService.registerMember(signupRequestDTO);

        // Then
        verify(passwordEncoder).encode("password123!");  // 패스워드 인코딩이 호출되었는지 확인
    }

    @Test
    void testMemberStatusAfterSignup() {
        // Given
        Member member = Member.builder()
                .memberId(1L)
                .email("user@gmail.com")
                .passwordHash("encodedPassword")
                .name("yoon")
                .sex(Sex.MALE)
                .birthDate(LocalDate.of(1990, 1, 1))
                .status(MemberStatus.ACTIVE)
                .build();

        when(memberRepository.existsByEmail(signupRequestDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member createdMember = signupMemberService.registerMember(signupRequestDTO);

        // Then
        assertEquals(MemberStatus.ACTIVE, createdMember.getStatus());
    }
}
