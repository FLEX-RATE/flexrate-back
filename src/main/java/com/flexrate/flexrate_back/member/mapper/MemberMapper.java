package com.flexrate.flexrate_back.member.mapper;

import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.dto.MemberSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
    public MemberSummaryDto toSummaryDto(Member member) {
        return MemberSummaryDto.builder()
                .id(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .sex(member.getSex())
                .birthDate(member.getBirthDate())
                .memberStatus(member.getStatus())
                .createdAt(member.getCreatedAt())
                .lastLoginAt(member.getLastLoginAt())
                // 현재 대출 서비스 이용 중 여부 (EXECUTED 상태인 경우)
                .hasLoan(member.getLoanApplication() != null && member.getLoanApplication().getStatus() == LoanApplicationStatus.EXECUTED)
                .loanCount(member.getLoanApplication() != null ? member.getLoanApplication().getLoanTransactions().size() : 0)
                .build();
    }
}
