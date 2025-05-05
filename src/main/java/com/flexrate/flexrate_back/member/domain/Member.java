package com.flexrate.flexrate_back.member.domain;

import com.flexrate.flexrate_back.member.enums.LoginMethod;
import com.flexrate.flexrate_back.member.enums.Role;
import com.flexrate.flexrate_back.member.enums.Sex;
import com.flexrate.flexrate_back.member.enums.MemberStatus;
import com.flexrate.flexrate_back.member.enums.*;
import com.flexrate.flexrate_back.loan.domain.LoanApplication;
import com.flexrate.flexrate_back.notification.domain.Notification;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

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

    @Column(nullable = false)
    private String passwordHash;

    private LocalDateTime passwordLastChangedAt;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = true, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDate birthDate;

    @CreatedDate
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    private LoginMethod lastLoginMethod;

    @Enumerated(EnumType.STRING)
    private ConsumeGoal consumeGoal;

    @Enumerated(EnumType.STRING)
    private ConsumptionType consumptionType;

    @OneToOne(mappedBy = "member")
    private LoanApplication loanApplication;

    @OneToMany(mappedBy = "member")
    private List<Notification> notifications;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateSex(Sex sex) {
        this.sex = sex;
    }

    public void updateBirthDate(LocalDate localDate) {
        this.birthDate = localDate;
    }

    public void updateMemberStatus(MemberStatus memberStatus) {
        this.status = memberStatus;
    }
}