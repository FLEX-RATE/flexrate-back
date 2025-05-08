package com.flexrate.flexrate_back.report.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;

@Component
public class ConsumptionReportApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getConsumptionSummary(Long memberId, YearMonth reportMonth) {
        String url = String.format(
                "https://openapi.example.com/analysis/summary?memberId=%d&month=%s",
                memberId, reportMonth.toString()
        );
        return restTemplate.getForObject(url, String.class);
    }
}
