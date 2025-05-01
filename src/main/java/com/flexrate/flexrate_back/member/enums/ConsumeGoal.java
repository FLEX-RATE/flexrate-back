package com.flexrate.flexrate_back.member.enums;

public enum ConsumeGoal {
    SAVING("절약형", new String[]{
            "식비를 일주일 단위로 예산화해 지출 기록하기",
            "불필요한 구독 서비스 1개 해지하기",
            "이번 달 온라인 쇼핑 횟수 2회 이내로 제한",
            "일주일에 한 번 '무지출 데이' 실천하기"
    });
    // 다른 ConsumeGoal 추가 가능

    private final String category;  // 소비성향 (예: 절약형)
    private final String[] goals;   // 소비목표 목록 (문장들)

    ConsumeGoal(String category, String[] goals) {
        this.category = category;
        this.goals = goals;
    }

    // 소비목표 문장 목록을 가져오는 메서드
    public String[] getGoals() {
        return goals;
    }

    // 소비성향(카테고리) 가져오는 메서드
    public String getCategory() {
        return category;
    }

    // 소비목표 문장을 기반으로 해당 목표가 어느 ConsumeGoal에 속하는지 찾는 메서드
    public static ConsumeGoal fromGoalSentence(String goalSentence) {
        for (ConsumeGoal goal : ConsumeGoal.values()) {
            for (String sentence : goal.getGoals()) {
                if (sentence.equals(goalSentence)) {
                    return goal;
                }
            }
        }
        return null;  // 문장에 해당하는 목표가 없으면 null 반환
    }

    // 기존의 fromCategory 메서드는 그대로 사용할 수 있음
    public static ConsumeGoal fromCategory(String category) {
        for (ConsumeGoal goal : ConsumeGoal.values()) {
            if (goal.getCategory().equals(category)) {
                return goal;
            }
        }
        return null;  // 해당 카테고리가 없으면 null 반환
    }
    }
