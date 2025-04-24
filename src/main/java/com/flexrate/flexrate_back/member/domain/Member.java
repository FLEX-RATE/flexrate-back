package com.flexrate.flexrate_back.member.domain;

import com.flexrate.flexrate_back.member.enums.LoginMethod;
import com.flexrate.flexrate_back.member.enums.UserStatus;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.notification.domain.Notification;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 50)
    private String passwordHash;

    private LocalDateTime passwordLastChangedAt;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    private LocalDate birthDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    private LoginMethod lastLoginMethod;

    @OneToOne(mappedBy = "member")
    private LoanApplication loanApplication;

    @OneToMany(mappedBy = "member")
    private List<Notification> notifications;
}
