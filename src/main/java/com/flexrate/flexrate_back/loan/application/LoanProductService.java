package com.flexrate.flexrate_back.loan.application;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import com.flexrate.flexrate_back.loan.application.repository.InterestRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanProductRepository;
import com.flexrate.flexrate_back.loan.application.repository.LoanTransactionRepository;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.domain.LoanProduct;
import com.flexrate.flexrate_back.loan.dto.LoanProductSummaryDto;
import com.flexrate.flexrate_back.loan.enums.LoanApplicationStatus;
import com.flexrate.flexrate_back.loan.enums.LoanType;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final MemberRepository memberRepository;
    private final InterestRepository interestRepository;
    private final LoanTransactionRepository loanTransactionRepository;


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
    @Transactional
    public void selectProduct(Long productId, Member member) {
        Member persistentMember = memberRepository.findById(member.getMemberId())
                .orElseThrow(() -> new FlexrateException(ErrorCode.USER_NOT_FOUND));
        Optional<LoanApplication> existing = loanApplicationRepository.findByMember(persistentMember);
        if (existing.isPresent()) {
            LoanApplication app = existing.get();
            if (app.getStatus() == LoanApplicationStatus.PRE_APPLIED) {
                if (app.getInterests() != null) interestRepository.deleteAll(app.getInterests());
                if (app.getLoanTransactions() != null) loanTransactionRepository.deleteAll(app.getLoanTransactions());

                loanApplicationRepository.flush();

                // member 연관관계 제거
                app.unlinkMember();

                loanApplicationRepository.delete(app);

                loanApplicationRepository.flush();
            } else {
                throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
            }
        }

        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new FlexrateException(ErrorCode.LOAN_PRODUCT_NOT_FOUND));

        LoanApplication application = LoanApplication.builder()
                .member(persistentMember)
                .product(product)
                .status(LoanApplicationStatus.PRE_APPLIED)
                .loanType(LoanType.NEW)
                .loanTransactions(new ArrayList<>())
                .interests(new ArrayList<>())
                .build();

        loanApplicationRepository.save(application);
    }
}
