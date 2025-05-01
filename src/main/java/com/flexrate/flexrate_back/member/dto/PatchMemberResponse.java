package com.flexrate.flexrate_back.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class PatchMemberResponse {
    private Long memberId;
    private String name;
    private Sex sex;
    private LocalDate birthDate;

    private MemberStatus memberStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
