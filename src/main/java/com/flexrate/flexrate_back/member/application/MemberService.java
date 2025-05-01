package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public Member findById(Long memberId) {
    return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Unexpected member"));
    }
}
