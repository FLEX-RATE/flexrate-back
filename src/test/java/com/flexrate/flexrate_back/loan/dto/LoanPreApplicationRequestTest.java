package com.flexrate.flexrate_back.loan.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoanPreApplicationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("LoanPreApplicationRequest - 정상 데이터로 검증 성공")
    void validateSuccess() {
        LoanPreApplicationRequest request = LoanPreApplicationRequest.builder()
                .businessType("IT")
                .employmentType("FULL_TIME")
                .hireDate("2022-01")
                .schoolName("서울대학교")
                .educationStatus("GRADUATED")
                .annualIncome(3500)
                .creditGrade("2")
                .residenceType("OWNED")
                .isBankrupt(false)
                .loanPurpose("BUSINESS")
                .build();

        Set<ConstraintViolation<LoanPreApplicationRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("LoanPreApplicationRequest - businessType이 빈 문자열이면 검증 실패")
    void validateFailBlankBusinessType() {
        LoanPreApplicationRequest request = LoanPreApplicationRequest.builder()
                .businessType("")
                .employmentType("FULL_TIME")
                .hireDate("2022-01")
                .schoolName("서울대학교")
                .educationStatus("GRADUATED")
                .annualIncome(3500)
                .creditGrade("2")
                .residenceType("OWNED")
                .isBankrupt(false)
                .loanPurpose("BUSINESS")
                .build();

        Set<ConstraintViolation<LoanPreApplicationRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }
}
