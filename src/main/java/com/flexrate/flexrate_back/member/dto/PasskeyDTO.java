package com.flexrate.flexrate_back.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasskeyDTO {
    private String id;
    private String key;
    private String method;
}
