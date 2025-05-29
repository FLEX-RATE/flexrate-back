package com.flexrate.flexrate_back.notification.domain.repository;

import com.flexrate.flexrate_back.notification.domain.Notification;
import com.flexrate.flexrate_back.notification.domain.QNotification;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findNotifications(Long memberId, Long lastNotificationId, int limit) {
        QNotification notification = QNotification.notification;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(notification.member.memberId.eq(memberId));
        if (lastNotificationId != null) {
            builder.and(notification.notificationId.lt(lastNotificationId));
        }

        return queryFactory.selectFrom(notification)
                .where(builder)
                .orderBy(notification.notificationId.desc())
                .limit(limit)
                .fetch();
    }
}
