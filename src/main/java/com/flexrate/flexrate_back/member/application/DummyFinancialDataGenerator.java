package com.flexrate.flexrate_back.member.application;

import com.flexrate.flexrate_back.financialdata.domain.UserFinancialData;
import com.flexrate.flexrate_back.financialdata.domain.repository.UserFinancialDataRepository;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialCategory;
import com.flexrate.flexrate_back.financialdata.enums.UserFinancialDataType;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DummyFinancialDataGenerator {

    private final UserFinancialDataRepository financialDataRepository;
    private final Random random = new Random();

    public void generateDummyFinancialData(Member member) {
        List<UserFinancialData> dummyDataList = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            // UserFinancialDataType에 따라 카테고리 설정
            UserFinancialDataType dataType = i % 2 == 0
                    ? UserFinancialDataType.INCOME
                    : UserFinancialDataType.EXPENSE;

            // 해당 유형에 맞는 카테고리 랜덤 선택
            UserFinancialCategory category = getRandomCategoryForType(dataType);

            // 더미 데이터 생성
            UserFinancialData data = UserFinancialData.builder()
                    .member(member)
                    .dataType(dataType)
                    .category(category)
                    .value(random.nextInt(500000) + 1000)  // 1000 ~ 100000 사이의 값 생성
                    .collectedAt(LocalDateTime.now().minusDays(random.nextInt(60)))  // 지난 60일 내 날짜 랜덤 생성
                    .build();

            dummyDataList.add(data);
        }

        // 더미 데이터 저장
        financialDataRepository.saveAll(dummyDataList);
    }

    private UserFinancialCategory getRandomCategoryForType(UserFinancialDataType type) {
        switch (type) {
            case INCOME:
                return getRandomCategoryForIncome();
            case EXPENSE:
                return getRandomCategoryForExpense();
            default:
                throw new FlexrateException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private UserFinancialCategory getRandomCategoryForIncome() {
        return getRandomCategoryFromList(UserFinancialCategory.FOOD, UserFinancialCategory.HEALTH, UserFinancialCategory.EDUCATION);
    }

    private UserFinancialCategory getRandomCategoryForExpense() {
        return getRandomCategoryFromList(UserFinancialCategory.LIVING, UserFinancialCategory.TRANSPORT, UserFinancialCategory.LEISURE);
    }

    private UserFinancialCategory getRandomCategoryForLoanBalance() {
        return getRandomCategoryFromList(UserFinancialCategory.COMMUNICATION, UserFinancialCategory.ETC);
    }

    private UserFinancialCategory getRandomCategoryFromList(UserFinancialCategory... categories) {
        List<UserFinancialCategory> categoryList = List.of(categories);
        return categoryList.get(random.nextInt(categoryList.size()));
    }
}
