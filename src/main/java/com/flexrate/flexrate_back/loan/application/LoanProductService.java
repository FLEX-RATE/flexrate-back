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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductService {
    private final LoanProductRepository loanProductRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final MemberRepository memberRepository;

    /**
     * 모든 대출 상품 목록을 조회합니다.
     *
     * @return LoanProductSummaryDto 리스트
     */
    public List<LoanProductSummaryDto> getAllProducts() {
        log.info("대출 상품 전체 목록 조회 요청");

        List<LoanProductSummaryDto> result = loanProductRepository.findAll().stream()
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

        log.info("대출 상품 전체 목록 조회 성공: 반환 건수: {}", result.size());
        return result;
    }

    /**
     * 사용자가 특정 대출 상품을 선택했을 때 LoanApplication을 미리 생성합니다.
     *
     * 기존 대출 신청이 존재할 경우:
     * 해당 신청의 status가 NONE이 아닌 경우 경우 FlexrateException 예외 발생
     *
     * @param productId 선택한 대출 상품 ID
     * @param member 선택한 사용자
     * @throws FlexrateException LOAN_APPLICATION_ALREADY_EXISTS 또는 LOAN_PRODUCT_NOT_FOUND 예외 발생 가능
     */
    @Transactional
    public void selectProduct(Long productId, Member member) {
        log.info("대출 상품 선택 요청: productId={}, memberId={}", productId, member != null ? member.getMemberId() : null);

        assert member != null;
        Member persistentMember = memberRepository.findById(member.getMemberId())
                .orElseThrow(() -> {
                    log.warn("대출 상품 선택 실패: 존재하지 않는 회원, memberId={}", member.getMemberId());
                    return new FlexrateException(ErrorCode.USER_NOT_FOUND);
                });

        LoanApplication app = loanApplicationRepository.findByMember(persistentMember)
                .orElseThrow(() -> {
                    log.warn("대출 상품 선택 실패: 기존 대출 신청 없음, memberId={}", persistentMember.getMemberId());
                    return new FlexrateException(ErrorCode.LOAN_NOT_FOUND);
                });

        if (app.getStatus() != LoanApplicationStatus.NONE) {
            log.warn("대출 상품 선택 실패: 이미 신청된 대출 존재, memberId={}, status={}", persistentMember.getMemberId(), app.getStatus());
            throw new FlexrateException(ErrorCode.LOAN_APPLICATION_ALREADY_EXISTS);
        }

        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("대출 상품 선택 실패: 존재하지 않는 상품, productId={}", productId);
                    return new FlexrateException(ErrorCode.LOAN_PRODUCT_NOT_FOUND);
                });

        app.patchStatus(LoanApplicationStatus.PRE_APPLIED);
        app.setProduct(product);

        log.info("대출 상품 선택 및 상태 변경 성공: memberId={}, productId={}, 변경 상태={}", persistentMember.getMemberId(), productId, app.getStatus());
    }
}
