package com.flexrate.flexrate_back.log.domain;

import com.flexrate.flexrate_back.auth.enums.AuthMethod;
import com.flexrate.flexrate_back.log.enums.EventType;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AuditLog")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private String action;

    private EventType eventType;

    private AuthMethod authMethod;

    @Column(length = 255)
    private String detail;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 100)
    private String deviceInfo;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}
