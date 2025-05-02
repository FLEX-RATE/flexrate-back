package com.flexrate.flexrate_back.member.enums;

import lombok.Getter;

import java.util.List;

@Getter
public enum ConsumeGoal {

    SAVING("최대한 아껴서 써요", List.of(
            "오늘 하루, 지출 없이 보내고싶어요.",
            "하루 식비로 10,000원 이상 쓰고싶지 않아요.",
            "수입 70% 이상을 저축해요",
            "지출보다 수익이 많아요"
    )),

    PRACTICAL("필요한 것만 써요", List.of(
            "대중교통만 이용해요",
            "구매하기 전에 가격을 비교해요",
            "주택청약을 들고 있어요",
            "10만원 이하의 의류를 소비해요."
    )),

    BALANCE("계획적으로 소비해요", List.of(
            "오늘은 하나의 카테고리에만 소비해보세요.",
            "매달 소액의 저축을 목표로 해요.",
            "필요하지 않은 가전제품은 구매하지 않아요.",
            "평균 지출의 10% 이상 소비해요."
    )),

    CONSUMER_ORIENTED("하고 싶은 건 하는 편이에요", List.of(
            "만원을 넘기는 디저트와 커피는 사치에요",
            "하루에 5만원 이상의 무리한 소비는 하지 않아요",
            "월 구독비로 5만원을 넘기지 않아요",
            "2만원이 넘는 밥값은 소비하지 않는 편이에요"
    ));

    private final String description;
    private final List<String> goals;

    ConsumeGoal(String description, List<String> goals) {
        this.description = description;
        this.goals = goals;
    }
}
