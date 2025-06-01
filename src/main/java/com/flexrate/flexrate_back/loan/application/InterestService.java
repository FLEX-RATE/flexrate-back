package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.InterestRepository;
import com.flexrate.flexrate_back.loan.domain.Interest;
import com.flexrate.flexrate_back.loan.dto.InterestAnalysisRequest;
import com.flexrate.flexrate_back.loan.dto.InterestResponse;
import com.flexrate.flexrate_back.loan.dto.InterestStatsDto;
import com.flexrate.flexrate_back.loan.dto.InterestSummaryResponse;
import com.flexrate.flexrate_back.loan.enums.PeriodType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {
    private final InterestRepository interestRepository;

    /**
     * 현재 대출 금리 변동 정보 조회
     */
    public InterestResponse getCurrentInterestChange(Long applicationId) {
        log.info("금리 변동 조회 요청: applicationId={}", applicationId);
        List<Interest> interests = interestRepository
                .findTop2ByLoanApplication_ApplicationIdOrderByInterestDateDesc(applicationId);

        // 현재 대출 금리가 없는 경우
        if (interests.isEmpty()) {
            log.warn("대출신청ID={}의 금리 정보가 존재하지 않음", applicationId);
            throw new FlexrateException(ErrorCode.NO_INTEREST);
        }

        // 현재 대출 금리가 하나인 경우
        if (interests.size() == 1) {
            log.info("대출신청ID={}의 금리 데이터가 1건만 존재, 변동률 0으로 반환", applicationId);
            return new InterestResponse(interests.get(0).getInterestRate(), 0, 0);
        }

        Interest latest = interests.get(0);
        Interest previous = interests.get(1);
        float change = ((latest.getInterestRate() - previous.getInterestRate()) / previous.getInterestRate()) * 100;
        log.info("대출신청ID={} 최신금리={}, 이전금리={}, 변동률={}", applicationId, latest.getInterestRate(), previous.getInterestRate(), change);

        return new InterestResponse(latest.getInterestRate(), previous.getInterestRate(), change);
    }

    /**
     * 금리 통계(일/주/월) 분석
     */
    public InterestSummaryResponse getInterestStats(InterestAnalysisRequest request) {
        Long applicationId = request.applicationId();
        PeriodType periodType = request.periodType();

        log.info("대출신청ID={}의 금리 통계 조회 요청, 기간단위={}", applicationId, periodType);

        List<Interest> interests = interestRepository.findByLoanApplication_ApplicationId(applicationId);
        if (interests.isEmpty()) {
            log.warn("대출신청ID={}의 금리 데이터가 없음", applicationId);
            throw new FlexrateException(ErrorCode.NO_INTEREST);
        }

        // 시간순 정렬
        interests.sort(Comparator.comparing(Interest::getInterestDate));

        Map<String, List<Interest>> grouped = new LinkedHashMap<>();
        DateTimeFormatter formatter;

        switch (periodType) {
            case DAILY -> formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            case WEEKLY -> formatter = DateTimeFormatter.ofPattern("YYYY-'W'ww");
            case MONTHLY -> formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            default -> {
                log.error("알 수 없는 기간 유형: {}", periodType);
                throw new IllegalArgumentException("알 수 없는 기간 유형입니다.");
            }
        }

        for (Interest interest : interests) {
            String key = interest.getInterestDate().format(formatter);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(interest);
        }

        List<InterestStatsDto> result = new ArrayList<>();
        Float previous = null;
        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        for (Map.Entry<String, List<Interest>> entry : grouped.entrySet()) {
            float avg = (float) entry.getValue().stream().mapToDouble(Interest::getInterestRate).average().orElse(0);
            max = Math.max(max, avg);
            min = Math.min(min, avg);

            Float change = null;
            if (previous != null && previous != 0) {
                change = ((avg - previous) / previous) * 100;
            }

            log.info("기간={} 평균금리={}, 변동률={}", entry.getKey(), avg, change);

            result.add(new InterestStatsDto(entry.getKey(), avg, change));
            previous = avg;
        }

        log.info("대출신청ID={} 금리통계 분석 완료. 구간수={}, 최고금리={}, 최저금리={}", applicationId, result.size(), max, min);

        return new InterestSummaryResponse(result, max, min);
    }
}