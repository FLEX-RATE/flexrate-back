package com.flexrate.flexrate_back.member.enums;

import com.flexrate.flexrate_back.member.dto.ConsumeGoalResponse.ConsumeGoalSummary;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum ConsumeGoal {
    NO_SPENDING_TODAY("오늘 하루, 지출 없이 보내고싶어요.", ConsumptionType.CONSERVATIVE),
    LIMIT_DAILY_MEAL("하루 식비로 10,000원 이상 쓰고싶지 않아요.", ConsumptionType.CONSERVATIVE),
    SAVE_70_PERCENT("수입 70% 이상을 저축해요.", ConsumptionType.CONSERVATIVE),
    INCOME_OVER_EXPENSE("지출보다 수익이 많아요.", ConsumptionType.CONSERVATIVE),

    ONLY_PUBLIC_TRANSPORT("대중교통만 이용해요.", ConsumptionType.PRACTICAL),
    COMPARE_BEFORE_BUYING("구매하기 전에 가격을 비교해요.", ConsumptionType.PRACTICAL),
    HAS_HOUSING_SAVING("주택청약을 들고 있어요.", ConsumptionType.PRACTICAL),
    CLOTHING_UNDER_100K("10만원 이하의 의류를 소비해요.", ConsumptionType.PRACTICAL),

    ONE_CATEGORY_SPEND("오늘은 하나의 카테고리에만 소비해보세요.", ConsumptionType.BALANCED),
    SMALL_MONTHLY_SAVE("매달 소액의 저축을 목표로 해요.", ConsumptionType.BALANCED),
    NO_USELESS_ELECTRONICS("필요하지 않은 가전제품은 구매하지 않아요.", ConsumptionType.BALANCED),
    OVER_10_PERCENT("평균 지출의 10% 이상 소비해요.", ConsumptionType.BALANCED),

    NO_EXPENSIVE_DESSERT("만원을 넘기는 디저트와 커피는 사치에요.", ConsumptionType.CONSUMPTION_ORIENTED),
    NO_OVER_50K_PER_DAY("하루에 5만원 이상의 무리한 소비는 하지 않아요.", ConsumptionType.CONSUMPTION_ORIENTED),
    SUBSCRIPTION_UNDER_50K("월 구독비로 5만원을 넘기지 않아요.", ConsumptionType.CONSUMPTION_ORIENTED),
    MEAL_UNDER_20K("2만원이 넘는 밥값은 소비하지 않는 편이에요.", ConsumptionType.CONSUMPTION_ORIENTED);

    private final String description;
    private final ConsumptionType type;

    /**
     * 특정 소비 유형에 해당하는 소비 목표 리스트 반환
     * @param type 소비 유형
     * @return 소비 목표 리스트(ConsumeGoal, description)
     */
    public static List<ConsumeGoalSummary> getConsumeGoalsByType(ConsumptionType type) {
        return Arrays.stream(values())
                .filter(goal -> goal.getType() == type)
                .map(goal -> new ConsumeGoalSummary(goal, goal.getDescription()))
                .collect(Collectors.toList());
    }
}
