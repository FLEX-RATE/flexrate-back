package com.flexrate.flexrate_back.notification.domain.repository;

import com.flexrate.flexrate_back.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMember_MemberIdOrderBySentAtDesc(Long memberId);
    void deleteByMember_MemberId(Long memberId);
}

