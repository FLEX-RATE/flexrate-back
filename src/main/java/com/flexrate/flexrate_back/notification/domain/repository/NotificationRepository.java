package com.flexrate.flexrate_back.notification.domain.repository;

import com.flexrate.flexrate_back.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    void deleteByMember_MemberId(Long memberId);

    int countByMember_MemberIdAndIsReadFalse(Long memberId);
}

