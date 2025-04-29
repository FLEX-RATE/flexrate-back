package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.member.dto.PatchMemberRequest;
import com.flexrate.flexrate_back.member.dto.PatchMemberResponse;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class MemberAdminServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AdminAuthChecker adminAuthChecker;

    @InjectMocks
    private MemberAdminService memberAdminService;

    public MemberAdminServiceTest() {
        MockitoAnnotations.openMocks(this);
    }


    /**
     * 관리자 권한으로 회원 정보 수정 테스트
     * @since 2025.04.28
     * 허연규
     * */

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .name("김영희")
                .birthDate(LocalDate.of(1980, 1, 1))
                .sex(Sex.FEMALE)
                .status(MemberStatus.SUSPENDED)
                .build();
    }

    @Test
    @DisplayName("MemberAdminService - 회원 수정 정상 응답 테스트")
    void patchMemberSuccess() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 10, 10))
                .sex(Sex.MALE)
                .memberStatus(MemberStatus.ACTIVE)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));

        // when
        PatchMemberResponse response = memberAdminService.patchMember(1L, request, "adminToken");

        // then
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getBirthDate()).isEqualTo(LocalDate.of(1990, 10, 10));
        assertThat(response.getSex()).isEqualTo(Sex.MALE);
        assertThat(response.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("MemberAdminService - 관리자 인증 실패")
    public void patchMemberFail_AdminTokenNotFound() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 10, 10))
                .sex(Sex.MALE)
                .memberStatus(MemberStatus.ACTIVE)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(false);

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.patchMember(1L, request, "NoToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ADMIN_AUTH_REQUIRED);
    }

    @Test
    @DisplayName("MemberAdminService - 유저가 존재하지 않음")
    public void patchMemberFail_UserNotFound() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder()
                .name("홍길동")
                .birthDate(LocalDate.of(1990, 10, 10))
                .sex(Sex.MALE)
                .memberStatus(MemberStatus.ACTIVE)
                .build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.patchMember(2L, request, "adminToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("MemberAdminService - 필수 입력값 누락")
    public void patchMemberFail_NullMember() {
        // given
        PatchMemberRequest request = PatchMemberRequest.builder().build();

        when(adminAuthChecker.isAdmin(any())).thenReturn(true);
        when(memberRepository.findById(any(Long.class))).thenReturn(Optional.of(member));

        // when & then
        FlexrateException exception = assertThrows(FlexrateException.class, () -> {
            memberAdminService.patchMember(1L, request, "adminToken");
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.AUTH_REQUIRED_FIELD_MISSING);
    }
}
