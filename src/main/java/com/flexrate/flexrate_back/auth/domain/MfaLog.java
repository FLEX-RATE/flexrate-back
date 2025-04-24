package com.flexrate.flexrate_back.auth.domain;

import com.flexrate.flexrate_back.auth.enums.MfaType;
import com.flexrate.flexrate_back.auth.enums.AuthResult;
import com.flexrate.flexrate_back.loan.domain.LoanTransaction;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MfaLog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mfaLogId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MfaType mfaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthResult result;

    @Column(nullable = false)
    private LocalDateTime authenticatedAt;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private LoanTransaction transaction;

    @Column(length = 20)
    private String deviceInfo;
}
