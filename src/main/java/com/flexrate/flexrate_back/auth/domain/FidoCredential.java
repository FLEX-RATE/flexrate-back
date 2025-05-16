package com.flexrate.flexrate_back.auth.domain;

import com.flexrate.flexrate_back.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "FidoCredential")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FidoCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long credentialId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 1000)
    private String publicKey;

    @Column(nullable = false)
    private long signCount;

    @Column
    private LocalDateTime lastUsedDate;

    @Column(nullable = false, length = 20)
    private String deviceInfo;

    private boolean isActive;
}
