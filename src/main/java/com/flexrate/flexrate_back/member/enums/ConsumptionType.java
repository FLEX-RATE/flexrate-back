package com.flexrate.flexrate_back.member.enums;

public enum ConsumptionType {
    // 소비 성향 정의
    절약형("식비를 일주일 단위로 예산화해 지출 기록하기\n" +
            "불필요한 구독 서비스 1개 해지하기\n" +
            "이번 달 온라인 쇼핑 횟수 2회 이내로 제한\n" +
            "일주일에 한 번 '무지출 데이' 실천하기"),

    균형형("카테고리별 소비 비중(식비/문화/쇼핑 등)을 점검하고 리밸런싱하기\n" +
            "고정 지출(구독, 통신 등)과 변동 지출 구분해 분석하기\n" +
            "장 보기 전 '예상 금액' 정하고 비교해보기\n" +
            "지난 달 소비 항목 중 하나를 줄여보기 (ex. 배달비 20% 절감)"),

    실용형("이번 달 '가장 만족한 소비 3가지' 기록하고 다음 달에도 유지\n" +
            "불필요한 재구매 방지를 위한 물건 관리 점검하기\n" +
            "쿠폰/할인 활용해 실속 소비 3건 이상 해보기\n" +
            "카드 사용내역에서 충동 구매 항목 1개 줄이기"),

    소비지향형("충동 구매 항목 1개 줄이고, 그 돈으로 '계획된 지출' 만들기\n" +
            "하루 소비 중 기억에 남은 소비 1건씩 메모하기 (소비 일기)\n" +
            "지난달보다 카드 지출 총액 10% 줄이기 도전\n" +
            "'원하는 소비' 전 24시간 재검토 체크리스트 사용해보기");

    private final String description;

    // 생성자
    ConsumptionType(String description) {
        this.description = description;
    }

    // 소비 성향에 대한 설명 반환
    public String getDescription() {
        return description;
    }
}
