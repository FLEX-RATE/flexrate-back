package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.common.dto.PaginationInfo;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.repository.MemberQueryRepository;
import com.flexrate.flexrate_back.member.dto.MemberSearchRequest;
import com.flexrate.flexrate_back.member.dto.MemberSearchResponse;
import com.flexrate.flexrate_back.member.mapper.MemberMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAdminService {
    private final MemberQueryRepository memberQueryRepository;
    private final MemberMapper memberMapper;
    private final AdminAuthChecker adminAuthChecker;

    /*
     * 관리자 권한으로 회원 목록 조회
     * @param request 검색 조건
     * @param adminToken 관리자 인증 토큰
     * @return MemberSearchResponse 회원 목록, 페이징 정보
     * @throws FlexrateException ErrorCode ADMIN_AUTH_REQUIRED 관리자 인증 필요
     * @since 2025.04.26
     * @author 권민지
     */
    public MemberSearchResponse searchMembers(@Valid MemberSearchRequest request, String adminToken) {
        // A007 관리자 인증 체크
        if (!adminAuthChecker.isAdmin(adminToken)) {
            throw new FlexrateException(ErrorCode.ADMIN_AUTH_REQUIRED);
        }

        // 기본 정렬 createdAt 내림차순
        Sort sort = request.sortBy() != null
                ? Sort.by(request.sortBy().name()).ascending()
                : Sort.by("createdAt").descending();

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                request.page() != null ? request.page() : 0,
                request.size() != null ? request.size() : 10,
                sort
        );

        var members = memberQueryRepository.searchMembers(request, pageable);

        return MemberSearchResponse.builder()
                .paginationInfo(new PaginationInfo(
                        members.getNumber(),
                        members.getSize(),
                        members.getTotalPages(),
                        members.getTotalElements()
                ))
                .members(members.getContent().stream()
                        .map(memberMapper::toSummaryDto)
                        .toList())
                .build();
    }
}
