package com.flexrate.flexrate_back.auth.domain;

import com.flexrate.flexrate_back.auth.enums.AuthMethod;
import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Authentication")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long authId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mfa_log_id")
    private MfaLog mfaLog;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credential_id")
    private FidoCredential fidoCredential;

    private LocalDateTime authenticatedAt;

    @Enumerated(EnumType.STRING)
    private AuthMethod authMethod;

    @Column(length = 20)
    private String ipAddress;

    @Column(length = 100)
    private String deviceInfo;
}
