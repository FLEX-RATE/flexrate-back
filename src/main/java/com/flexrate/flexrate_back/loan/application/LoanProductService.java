package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanProductRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanProduct;
import com.flexrate.flexrate_back.loan.dto.LoanProductSummaryDto;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.enums.LoanType;
import com.flexrate.flexrate_back.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
/**
 * 대출 상품 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 *
 * @since 2025.05.05
 * @author 유승한
 */
@Service
@RequiredArgsConstructor
public class LoanProductService {
    private final LoanProductRepository loanProductRepository;
    private final LoanApplicationRepository loanApplicationRepository;


    /**
     * 모든 대출 상품 목록을 조회합니다.
     *
     * @return LoanProductSummaryDto 리스트
     */
    public List<LoanProductSummaryDto> getAllProducts() {
        return loanProductRepository.findAll().stream()
                .map(product -> new LoanProductSummaryDto(
                        product.getProductId(),
                        product.getName(),
                        product.getDescription(),
                        product.getMaxAmount(),
                        product.getMinRate(),
                        product.getMaxRate(),
                        product.getTerms()
                ))
                .toList();
    }

    /**
     * 사용자가 특정 대출 상품을 선택했을 때 LoanApplication을 미리 생성합니다.
     *
     * 기존 대출 신청이 존재할 경우:
     * 해당 신청의 status가 PRE_APPLIED인 경우 삭제 후 생성
     * 그 외의 경우 FlexrateException 예외 발생
     *
     * @param productId 선택한 대출 상품 ID
     * @param member 선택한 사용자
     * @throws FlexrateException LOAN_APPLICATION_ALREADY_EXISTS 또는 LOAN_PRODUCT_NOT_FOUND 예외 발생 가능
     */
    public void selectProduct(Long productId, Member member) {
        Optional<LoanApplication> existing = loanApplicationRepository.findByMember(member);
        if (existing.isPresent()) {
            LoanApplication app = existing.get();
            if (LoanApplicationStatus.PRE_APPLIED == app.getStatus()) {
                loanApplicationRepository.delete(app);
                loanApplicationRepository.flush();
            } else {
                throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
            }
        }

        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_PRODUCT_NOT_FOUND));

        LoanApplication application = LoanApplication.builder()
                .member(member)
                .product(product)
                .status(LoanApplicationStatus.PRE_APPLIED)
                .loanType(LoanType.NEW)
                .build();

        loanApplicationRepository.save(application);
    }

}
