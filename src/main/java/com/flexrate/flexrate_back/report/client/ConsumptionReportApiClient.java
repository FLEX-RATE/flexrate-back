package com.flexrate.flexrate_back.report.client;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.financialdata.domain.repository.UserFinancialDataRepository;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import com.flexrate.flexrate_back.report.dto.ChatGPTRequest;
import com.flexrate.flexrate_back.report.dto.ChatGPTResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsumptionReportApiClient {

    private final MemberRepository memberRepository;
    private final UserFinancialDataRepository userFinancialDataRepository;

    @Qualifier("openAiRestClient")
    private final RestClient restClient;

    @Value("${openai.model}")
    private String model;

    private static final String BASE_PROMPT = """
            아래는 특정 사용자의 소비 데이터입니다.
            소비 성향을 요약하고, 소비 목표에 맞춘 개선 방법을 제시해주세요.
            소비 항목에 대한 단순 나열이 아니라,
            줄글 형태로 이해하기 쉽게 요약해 주세요.

            소비 성향: %s
            소비 목표: %s

            소비 데이터:
            %s
            """;

    public String createConsumptionSummary(Long memberId, YearMonth reportMonth) {
        if (memberId == null || reportMonth == null) {
            throw new FlexrateException(ErrorCode.REPORT_MEMBER_OR_MONTH_NULL);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));

        String consumeGoal = member.getConsumeGoal().getDescription();
        String consumptionType = member.getConsumptionType().getDescription();

        String financialHistory = buildFinancialHistory(member, reportMonth);
        if (financialHistory.isEmpty()) {
            throw new FlexrateException(ErrorCode.NO_CONSUMPTION_DATA);
        }

        String prompt = String.format(BASE_PROMPT, consumptionType, consumeGoal, financialHistory);
        ChatGPTRequest request = new ChatGPTRequest(model, prompt);

        try {
            ChatGPTResponse response = restClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(ChatGPTResponse.class);
            System.out.println(response);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new FlexrateException(ErrorCode.OPENAI_ERROR);
            }

            return response.choices().get(0).message().content();

        } catch (Exception e) {
            throw new FlexrateException(ErrorCode.OPENAI_ERROR, e);
        }
    }

    private String buildFinancialHistory(Member member, YearMonth reportMonth) {
        List<UserFinancialData> financialData = userFinancialDataRepository.findAllByMember(member);
        StringBuilder builder = new StringBuilder();
        for (UserFinancialData data : financialData) {
            YearMonth dataMonth = YearMonth.from(data.getCollectedAt());
            if (dataMonth.equals(reportMonth)) {
                builder.append(String.format("- [%s] %s: %,d원\n",
                        data.getDataType(),
                        data.getCategory(),
                        data.getValue()));
            }
        }
        return builder.toString();
    }
}
