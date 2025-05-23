package com.flexrate.flexrate_back.report.dto;

import java.util.List;

public record ChatGPTResponse(
        List<Choice> choices
) {
    public record Choice(
            int index,
            Message message
    ) {}
}