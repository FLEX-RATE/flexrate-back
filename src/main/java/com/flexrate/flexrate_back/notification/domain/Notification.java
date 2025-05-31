package com.flexrate.flexrate_back.notification.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flexrate.flexrate_back.notification.enums.NotificationType;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 50)
    private String content;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean isRead;

    public void markAsRead() {
        this.isRead = true;
    }
}
