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
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InterestService {
    private final InterestRepository interestRepository;

    public InterestResponse getCurrentInterestChange(Long applicationId) {
        List<Interest> interests = interestRepository
                .findTop2ByLoanApplication_ApplicationIdOrderByInterestDateDesc(applicationId);

        // 현재 대출 금리가 하나인 경우
        if (interests.size() == 1) {
            return new InterestResponse(interests.get(0).getInterestRate(), 0, 0);
        }

        // 현재 대출 금리가 없는 경우
        if (interests.isEmpty()) {
            throw new FlexrateException(ErrorCode.NO_INTEREST);
        }

        Interest latest = interests.get(0);
        Interest previous = interests.get(1);

        float change = ((latest.getInterestRate() - previous.getInterestRate()) / previous.getInterestRate()) * 100;

        return new InterestResponse(latest.getInterestRate(), previous.getInterestRate(), change);
    }

    public InterestSummaryResponse getInterestStats(InterestAnalysisRequest request) {
        Long applicationId = request.applicationId();
        PeriodType periodType = request.periodType();

        List<Interest> interests = interestRepository.findByLoanApplication_ApplicationId(applicationId);
        if (interests.isEmpty()) {
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
            default -> throw new IllegalArgumentException("알 수 없는 기간 유형입니다.");
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

            result.add(new InterestStatsDto(entry.getKey(), avg, change));
            previous = avg;
        }

        return new InterestSummaryResponse(result, max, min);
    }

}