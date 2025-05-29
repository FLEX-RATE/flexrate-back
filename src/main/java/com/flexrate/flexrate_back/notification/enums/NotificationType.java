package com.flexrate.flexrate_back.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    LOAN_APPROVAL("%s님, 대출 신청이 승인되었습니다. 지금 확인해보세요."),
    LOAN_REJECTED("%s님, 대출 신청이 거절되었습니다. 지금 확인해보세요."),
    INTEREST_RATE_CHANGE("%s님, 금리가 변동되었습니다. 지금 확인해보세요."),
    MATURITY_NOTICE("%s님, 대출이 %s에 만기됩니다. 자세한 내용을 확인해보세요.");

    private final String messageTemplate;

    public String formatMessage(Object... args) {
        return String.format(messageTemplate, (Object[]) args);
    }
}
