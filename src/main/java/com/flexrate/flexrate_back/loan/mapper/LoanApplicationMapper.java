//package com.flexrate.flexrate_back.loan.mapper;
//
//import com.flexrate.flexrate_back.loan.domain.LoanApplication;
//import com.flexrate.flexrate_back.loan.dto.LoanApplicationRequest;
//import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationRequest;
//import com.flexrate.flexrate_back.loan.dto.LoanReviewApplicationResponse;
//import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
//import com.flexrate.flexrate_back.member.domain.Member;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//public class LoanApplicationMapper {
//    public void updateWithReviewResponse(
//            LoanApplication loanApplication,
//            LoanReviewApplicationResponse response
//    ) {
//        loanApplication.patchStatus(LoanApplicationStatus.PENDING);
//        loanApplication.setTotalAmount(response.getLoanLimit());
//        loanApplication.setRemainAmount(response.getLoanLimit());
//        loanApplication.setRate(response.getInitialRate());
//        loanApplication.setCreditScore(response.getCreditScore());
//        loanApplication.setAppliedAt(LocalDateTime.now());
//    }
//}
