package com.flexrate.flexrate_back.report.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;

@Component
public class ConsumptionReportApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 외부 OpenAPI를 통해 특정 회원의 소비 데이터를 요약한 리포트 조회
     * @param memberId 외부 API 요청에 사용될 회원 ID
     * @param reportMonth 조회 대상 월 (yyyy-MM)
     * @return 소비 리포트 요약 문자열
     * @since 2025.05.08
     * @author 서채연
     */
    public String getConsumptionSummary(Long memberId, YearMonth reportMonth) {
        String url = String.format(
                "https://openapi.example.com/analysis/summary?memberId=%d&month=%s",
                memberId, reportMonth.toString()
        );
        return restTemplate.getForObject(url, String.class);
    }
}
