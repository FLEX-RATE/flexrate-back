package com.flexrate.flexrate_back.notification.scheduler;

import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.loan.application.repository.LoanApplicationRepository;
import com.flexrate.flexrate_back.notification.enums.NotificationType;
import com.flexrate.flexrate_back.notification.event.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaturityNotificationScheduler {

    private final LoanApplicationRepository loanApplicationRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    /**
     * 매일 오전 9시에 만기일 30일, 7일, 3일, 2일, 1일 전, 당일인 대출을 찾아 알림을 발송
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMaturityNotifications() {
        LocalDate today = LocalDate.now();
        int[] daysBefore = {30, 7, 3, 2, 1, 0};

        for (int days : daysBefore) {
            LocalDate targetDate = today.plusDays(days);
            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

            List<LoanApplication> loans = loanApplicationRepository.findByEndDateBetween(startOfDay, endOfDay);

            for (LoanApplication loan : loans) {
                try {
                    String dateMessage;

                    if (days == 0) {
                        dateMessage = "오늘 내";
                    } else {
                        dateMessage = loan.getEndDate().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    }

                    notificationEventPublisher.sendMaturityNotification(
                            loan,
                            NotificationType.MATURITY_NOTICE,
                            dateMessage
                    );

                    log.info("만기 알림 발송 완료 - loanId: {}, member: {}, 만기일: {}, 알림타입: {}일전",
                            loan.getApplicationId(), loan.getMember().getName(), loan.getEndDate(), days);
                } catch (Exception e) {
                    log.error("만기 알림 발송 실패 - loanId: {}", loan.getApplicationId(), e);
                }
            }
        }
    }
}