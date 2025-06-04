-- MEMBER (회원)
create table member
(
    age                      int                                                                                                                                                                                                                                                                                                                                                                         null,
    birth_date               date                                                                                                                                                                                                                                                                                                                                                                        null,
    created_at               datetime(6)                                                                                                                                                                                                                                                                                                                                                                 null,
    last_login_at            datetime(6)                                                                                                                                                                                                                                                                                                                                                                 null,
    member_id                bigint auto_increment primary key,
    password_last_changed_at datetime(6)                                                                                                                                                                                                                                                                                                                                                                 null,
    updated_at               datetime(6)                                                                                                                                                                                                                                                                                                                                                                 null,
    name                     varchar(20)                                                                                                                                                                                                                                                                                                                                                                 not null,
    phone                    varchar(20)                                                                                                                                                                                                                                                                                                                                                                 null,
    email                    varchar(50)                                                                                                                                                                                                                                                                                                                                                                 not null,
    password_hash            varchar(255)                                                                                                                                                                                                                                                                                                                                                                not null,
    consume_goal             enum ('CLOTHING_UNDER_100K', 'COMPARE_BEFORE_BUYING', 'HAS_HOUSING_SAVING', 'INCOME_OVER_EXPENSE', 'LIMIT_DAILY_MEAL', 'MEAL_UNDER_20K', 'NO_EXPENSIVE_DESSERT', 'NO_OVER_50K_PER_DAY', 'NO_SPENDING_TODAY', 'NO_USELESS_ELECTRONICS', 'ONE_CATEGORY_SPEND', 'ONLY_PUBLIC_TRANSPORT', 'OVER_10_PERCENT', 'SAVE_70_PERCENT', 'SMALL_MONTHLY_SAVE', 'SUBSCRIPTION_UNDER_50K') null,
    consumption_type         enum ('BALANCED', 'CONSERVATIVE', 'CONSUMPTION_ORIENTED', 'PRACTICAL')                                                                                                                                                                                                                                                                                                      null,
    last_login_method        enum ('PASSKEY', 'PASSWORD', 'SOCIAL')                                                                                                                                                                                                                                                                                                                                      null,
    role                     enum ('ADMIN', 'MEMBER')                                                                                                                                                                                                                                                                                                                                                    not null,
    sex                      enum ('FEMALE', 'MALE')                                                                                                                                                                                                                                                                                                                                                     not null,
    status                   enum ('ACTIVE', 'SUSPENDED', 'WITHDRAWN')                                                                                                                                                                                                                                                                                                                                   not null
);

-- MEMBER_CREDIT_SUMMARY (회원 신용정보 산정 이력)
create table member_credit_summary
(
    summary_id                 bigint auto_increment primary key,
    application_id             bigint       not null,
    calculated_at              datetime(6)  not null,                          -- 평가 시점
    total_loan_count           int          not null default 0,                -- 보유 대출 건수
    active_loan_count          int          not null default 0,                -- 현재 상환 중인 대출 건수
    total_loan_balance         int          not null default 0,                -- 전체 대출 잔액
    total_loan_overdue_30d     int          not null default 0,                -- 30일 이상 연체 건수
    total_loan_overdue_90d     int          not null default 0,                -- 90일 이상 연체 건수
    has_current_overdue        boolean      not null default false,            -- 현재 연체 여부
    last_overdue_date          date         null,                              -- 최근 연체 발생일
    comm_overdue_count         int          not null default 0,                -- 통신비 연체 건수
    comm_overdue_max_days      int          not null default 0,                -- 통신비 최장 연체일수
    utility_overdue_count      int          not null default 0,                -- 공과금 연체 건수
    utility_overdue_max_days   int          not null default 0,                -- 공과금 최장 연체일수
    credit_score               int          not null,                          -- 신용점수(산정 결과)
    remark                     varchar(255) null,                              -- 비고
    constraint FK_member_credit_summary_member foreign key (application_id) references loan_application (application_id)
);

-- LOAN_PRODUCT (대출 상품)
create table loan_product
(
    max_amount  int          not null,
    max_rate    float        not null,
    min_rate    float        not null,
    terms       int          not null,
    product_id  bigint auto_increment primary key,
    name        varchar(20)  not null,
    description varchar(255) not null
);

-- LOAN_APPLICATION (대출 신청)
create table loan_application
(
    credit_score   int                                                                  not null,
    rate           float                                                                not null,
    remain_amount  int                                                                  not null,
    total_amount   int                                                                  not null,
    application_id bigint auto_increment primary key,
    applied_at     datetime(6)                                                          null,
    end_date       datetime(6)                                                          null,
    executed_at    datetime(6)                                                          null,
    member_id      bigint                                                               not null,
    product_id     bigint                                                               null,
    start_date     datetime(6)                                                          null,
    loan_type      enum ('EXTENSION', 'NEW', 'REJOIN')                                  not null,
    status         enum ('COMPLETED', 'EXECUTED', 'PENDING', 'PRE_APPLIED', 'REJECTED') not null,
    constraint UK_loan_application_member unique (member_id),
    constraint FK_loan_application_member foreign key (member_id) references member (member_id),
    constraint FK_loan_application_product foreign key (product_id) references loan_product (product_id)
);

-- FIDO_CREDENTIAL (패스키)
create table fido_credential
(
    is_active      bit           not null,
    sign_count     bigint        not null,
    credential_id  bigint auto_increment primary key,
    last_used_date datetime(6)   null,
    member_id      bigint        not null,
    device_info    varchar(20)   not null,
    public_key     varchar(1000) not null,
    constraint UK_fido_credential_member unique (member_id),
    constraint FK_fido_credential_member foreign key (member_id) references member (member_id)
);

-- LOAN_TRANSACTION (대출 거래)
create table loan_transaction
(
    amount         double                                   not null,
    application_id bigint                                   not null,
    member_id      bigint                                   not null,
    occurred_at    datetime(6)                              not null,
    transaction_id bigint auto_increment primary key,
    status         enum ('CANCELLED', 'FAILED', 'NORMAL')   not null,
    type           enum ('DELAY', 'EXECUTION', 'REPAYMENT') not null,
    constraint FK_loan_transaction_member foreign key (member_id) references member (member_id),
    constraint FK_loan_transaction_application foreign key (application_id) references loan_application (application_id)
);

-- MFA_LOG (다중 인증 로그)
create table mfa_log
(
    authenticated_at datetime(6)                            not null,
    mfa_log_id       bigint auto_increment primary key,
    transaction_id   bigint                                 null,
    device_info      varchar(20)                            null,
    mfa_type         enum ('EMAIL', 'FIDO2', 'PASS', 'SMS') not null,
    result           enum ('FAILURE', 'SUCCESS')            not null,
    constraint FK_mfa_log_transaction foreign key (transaction_id) references loan_transaction (transaction_id)
);

-- INTEREST (변동 금리)
create table interest
(
    interest_rate  float       not null,
    application_id bigint      not null,
    interest_date  datetime(6) not null,
    interest_id    bigint auto_increment primary key,
    constraint FK_interest_loan_application foreign key (application_id) references loan_application (application_id)
);

-- NOTIFICATION (알림)
create table notification
(
    is_read         bit                                       not null,
    member_id       bigint                                    not null,
    notification_id bigint auto_increment primary key,
    sent_at         datetime(6)                               not null,
    content         varchar(50)                               not null,
    type            enum ('LOAN_APPROVAL', 'MATURITY_NOTICE', 'INTEREST_RATE_CHANGE') not null,
    constraint FK_notification_member foreign key (member_id) references member (member_id)
);

-- REFRESH_TOKEN (리프레시 토큰)
create table refresh_token
(
    id            bigint auto_increment primary key,
    member_id     bigint       not null,
    refresh_token varchar(255) not null,
    constraint UK_refresh_token_member unique (member_id)
);

-- USER_FINANCIAL_DATA (유저 재무 데이터)
create table user_financial_data
(
    value        int                                                                                            not null,
    collected_at datetime(6)                                                                                    not null,
    data_id      bigint auto_increment primary key,
    user_id      bigint                                                                                         not null,
    category     enum ('COMMUNICATION', 'EDUCATION', 'ETC', 'FOOD', 'HEALTH', 'LEISURE', 'LIVING', 'TRANSPORT') null,
    data_type    enum ('EXPENSE', 'INCOME')                                                     not null,
    constraint FK_user_financial_data_member foreign key (user_id) references member (member_id)
);

-- CONSUMPTION_HABIT_REPORT (소비 습관 리포트)
create table consumption_habit_report
(
    created_at   date         not null,
    report_month varchar(7)   not null,
    member_id    bigint       not null,
    report_id    bigint auto_increment primary key,
    summary      varchar(500) null,
    constraint UK_consumption_habit_report_member_report_month unique (member_id, report_month),
    constraint FK_consumption_habbit_report_member foreign key (member_id) references member (member_id)
);

-- CONSUMPTION_HABIT_CATEGORY (소비 습관 카테고리)
create table consumption_habit_category
(
    category_id   bigint auto_increment primary key,
    report_id     bigint       not null,
    category      enum ('COMMUNICATION', 'EDUCATION', 'ETC', 'FOOD', 'HEALTH', 'LEISURE', 'LIVING', 'TRANSPORT') not null,
    amount        bigint       not null,
    ratio         decimal(5,2) not null,
    constraint FK_report_category foreign key (report_id) references consumption_habit_report (report_id) on delete cascade
);

-- AUDIT_LOG (감사 로그)
create table audit_log
(
    auth_method tinyint      null,
    event_type  tinyint      null,
    log_id      bigint auto_increment primary key,
    member_id   bigint       null,
    occurred_at datetime(6)  not null,
    ip_address  varchar(50)  null,
    device_info varchar(100) null,
    action      varchar(255) not null,
    detail      varchar(255) null,
    constraint FK_audit_log_member foreign key (member_id) references member (member_id),
    check (`auth_method` between 0 and 1),
    check (`event_type` between 0 and 2)
);

-- AUTHENTICATION (인증)
create table authentication
(
    auth_id          bigint auto_increment primary key,
    authenticated_at datetime(6)          null,
    credential_id    bigint               null,
    member_id        bigint               not null,
    mfa_log_id       bigint               null,
    ip_address       varchar(20)          null,
    device_info      varchar(100)         null,
    auth_method      enum ('FIDO', 'MFA') null,
    constraint UK_authentication_credential unique (credential_id),
    constraint UK_authentication_mfa_log unique (mfa_log_id),
    constraint FK_authentication_fido_credential foreign key (credential_id) references fido_credential (credential_id),
    constraint FK_authentication_mfa_log foreign key (mfa_log_id) references mfa_log (mfa_log_id),
    constraint FK_authentication_member foreign key (member_id) references member (member_id)
);