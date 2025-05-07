package com.flexrate.flexrate_back.member.dto;

import com.flexrate.flexrate_back.member.enums.ConsumeGoal;
import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
public record MypageUpdateRequest (
        @Email(message = "이메일 형식이 아닙니다.")
        String email,
        ConsumeGoal consumeGoal
) {}
