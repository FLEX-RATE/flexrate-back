package com.flexrate.flexrate_back.report.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.report.domain.ConsumptionHabitReport;
import com.flexrate.flexrate_back.report.domain.repository.ConsumptionHabitReportRepository;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryRatioResponse;
import com.flexrate.flexrate_back.report.dto.ConsumptionCategoryStatsResponse;
import com.flexrate.flexrate_back.financialdata.domain.repository.UserFinancialDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportStatisticsService {

    private final UserFinancialDataRepository financialDataRepository;
    private final ConsumptionHabitReportRepository reportRepository;
    private final ObjectMapper objectMapper;

    /**
     * 특정 회원의 지정 월에 대한 카테고리별 소비 통계 조회
     * @param member 조회 대상 회원
     * @param month 조회할 연월 (yyyy-MM)
     * @return 해당 회원의 카테고리별 소비 통계 응답 DTO
     * @since 2025.05.08
     * @author 서채연
     */
    public ConsumptionCategoryStatsResponse getCategoryStats(Member member, YearMonth month) {
        ConsumptionHabitReport report = reportRepository.findByMemberAndReportMonth(member, month)
                .orElseThrow(() -> new FlexrateException(ErrorCode.REPORT_DOESNT_EXISTS));
        log.info("리포트 객체 조회 :\nmemberId={}, yearMonth={}", member.getMemberId(), month);

        List<ConsumptionCategoryRatioResponse> stats;

        // consumptions가 비어 있거나 null이면 계산 및 저장
        if (report.getConsumptions() == null || report.getConsumptions().isBlank()) {
            log.info("기존에 계산한 소비 비율 존재하지 않음: reportId={}", report.getReportId());

            stats = financialDataRepository.findCategoryStatsWithRatio(member, month);
            try {
                String consumptionsJson = objectMapper.writeValueAsString(stats);
                report.setConsumptions(consumptionsJson);
                reportRepository.save(report);
                log.info("report 상에 consumption으로 계산한 소비 비율 json type으로 저장 : reportId={}", report.getReportId());
            } catch (JsonProcessingException e) {
                log.warn("소비 데이터 직렬화 실패 : reportId={}", report.getReportId());
                throw new FlexrateException(ErrorCode.JSON_SERIALIZATION_ERROR, e);
            }
        } else {
            try {
                stats = objectMapper.readValue(
                        report.getConsumptions(),
                        new TypeReference<>() {}
                );
                log.info("소비 데이터 역직렬화 성공 : reportId={}", report.getReportId());
            } catch (JsonProcessingException e) {
                log.warn("소비 데이터 역직렬화 실패 : reportId={}", report.getReportId());
                throw new FlexrateException(ErrorCode.JSON_DESERIALIZATION_ERROR, e);
            }
        }
        return new ConsumptionCategoryStatsResponse(
                member.getMemberId(),
                month,
                stats
        );
    }
}
