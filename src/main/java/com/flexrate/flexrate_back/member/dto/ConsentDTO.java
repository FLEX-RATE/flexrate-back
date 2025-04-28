package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsentDTO {
    private String agreementName;
    private boolean agreed;
}
