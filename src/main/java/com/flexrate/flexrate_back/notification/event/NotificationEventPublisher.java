package com.flexrate.flexrate_back.notification.event;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.notification.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    /**
     * 알림 발송 공통 메서드
     * @param member 대상 회원
     * @param notificationType 알림 타입
     * @param referenceId 참조 ID
     * @param args 메시지 포맷팅에 필요한 파라미터들
     */
    private void sendNotification(Member member, NotificationType notificationType, Long referenceId, Object... args) {
        // 알림은 실패해도 메인 로직에 영향 안 주도록
        try {
            Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = member.getName();
            System.arraycopy(args, 0, newArgs, 1, args.length);

            String content = notificationType.formatMessage(newArgs);

            NotificationEvent notificationEvent = new NotificationEvent(
                    this,
                    member,
                    notificationType,
                    content
            );

            eventPublisher.publishEvent(notificationEvent);
            log.info("알림 이벤트 발행 완료 - referenceId={}, type={}, member={}",
                    referenceId, notificationType, member.getName());
        } catch (Exception e) {
            log.error("알림 이벤트 발행 실패 - referenceId={}, type={}, member={}",
                    referenceId, notificationType, member.getName(), e);
        }
    }
    // 대출 알림
    public void sendLoanNotification(LoanApplication loanApplication, NotificationType type, Object... args) {
        sendNotification(loanApplication.getMember(), type, loanApplication.getApplicationId(), args);
    }
    // 금리 변동 알림
    public void sendInterestNotification(Member member, Long interestRateId, NotificationType type, Object... args) {
        sendNotification(member, type, interestRateId, args);
    }
    // 만기일 알림
    public void sendMaturityNotification(LoanApplication loanApplication, NotificationType type, Object... args) {
        sendNotification(loanApplication.getMember(), type, loanApplication.getApplicationId(), args);
    }
}
