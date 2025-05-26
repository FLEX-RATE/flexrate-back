package com.flexrate.flexrate_back.notification.event;

import com.flexrate.flexrate_back.member.domain.Member;
import com.flexrate.flexrate_back.notification.enums.NotificationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {
    private final Member member;
    private final NotificationType type;
    private final String content;

    public NotificationEvent(Object source, Member member, NotificationType type, String content) {
        super(source);
        this.member = member;
        this.type = type;
        this.content = content;
    }
}