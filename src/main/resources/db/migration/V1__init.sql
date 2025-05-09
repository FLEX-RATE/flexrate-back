-- MEMBER (회원)
create table member
(
    birth_date               date null,
    created_at               datetime(6) null,
    last_login_at            datetime(6) null,
    member_id                bigint auto_increment primary key,
    password_last_changed_at datetime(6) null,
    updated_at               datetime(6) null,
    name                     varchar(20) not null,
    phone                    varchar(20) null,
    email                    varchar(50) not null,
    password_hash            varchar(255) not null,
    consume_goal             enum ('CLOTHING_UNDER_100K', 'COMPARE_BEFORE_BUYING', 'HAS_HOUSING_SAVING', 'INCOME_OVER_EXPENSE', 'LIMIT_DAILY_MEAL', 'MEAL_UNDER_20K', 'NO_EXPENSIVE_DESSERT', 'NO_OVER_50K_PER_DAY', 'NO_SPENDING_TODAY', 'NO_USELESS_ELECTRONICS', 'ONE_CATEGORY_SPEND', 'ONLY_PUBLIC_TRANSPORT', 'OVER_10_PERCENT', 'SAVE_70_PERCENT', 'SMALL_MONTHLY_SAVE', 'SUBSCRIPTION_UNDER_50K') null,
    consumption_type         enum ('BALANCED', 'CONSERVATIVE', 'CONSUMPTION_ORIENTED', 'PRACTICAL') null,
    last_login_method        enum ('PASSKEY', 'PASSWORD', 'SOCIAL') null,
    role                     enum ('ADMIN', 'MEMBER') not null,
    sex                      enum ('FEMALE', 'MALE') not null,
    status                   enum ('ACTIVE', 'SUSPENDED', 'WITHDRAWN') not null
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
    constraint UKnu7i3b6jnfvmb18oufct4mbv0 unique (member_id),
    constraint FKeewfg5rtu0ux0gv0n357t6g1v foreign key (member_id) references member (member_id),
    constraint FKeyryxaov7qn04ctajae14l9sf foreign key (product_id) references loan_product (product_id)
);

-- FIDO_CREDENTIAL (패스키)
create table fido_credential
(
    is_active      bit         not null,
    sign_count     int         not null,
    credential_id  bigint auto_increment primary key,
    last_used_date datetime(6) null,
    member_id      bigint      not null,
    device_info    varchar(20) not null,
    public_key     varchar(50) not null,
    constraint UKmsct6biuq32sdb2e8icuu4t68 unique (member_id),
    constraint FKs3bknrmcmske2xkvd96ga2myj foreign key (member_id) references member (member_id)
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
    constraint FK8gityivfar00t9fc7lgyqx0s foreign key (member_id) references member (member_id),
    constraint FKntk7t4npuvm0ryaq2wl32514w foreign key (application_id) references loan_application (application_id)
);

-- MFA_LOG (다중 인증 로그)
create table mfa_log
(
    authenticated_at datetime(6)                   not null,
    mfa_log_id       bigint auto_increment primary key,
    transaction_id   bigint                        null,
    device_info      varchar(20)                   null,
    mfa_type         enum ('EMAIL', 'PASS', 'SMS') not null,
    result           enum ('FAILURE', 'SUCCESS')   not null,
    constraint FKkytl8f2c6q4ry0eihdyvwn3k1 foreign key (transaction_id) references loan_transaction (transaction_id)
);

-- INTEREST (대출 이자)
create table interest
(
    interest_rate  float       not null,
    application_id bigint      not null,
    interest_date  datetime(6) not null,
    interest_id    bigint auto_increment primary key,
    constraint FK71miynswoeucff7manm0xth9g foreign key (application_id) references loan_application (application_id)
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
    constraint FK1xep8o2ge7if6diclyyx53v4q foreign key (member_id) references member (member_id)
);

-- REFRESH_TOKEN (리프레시 토큰)
create table refresh_token
(
    id            bigint auto_increment primary key,
    member_id     bigint       not null,
    refresh_token varchar(255) not null,
    constraint UKdnbbikqdsc2r2cee1afysqfk9 unique (member_id)
);

-- USER_FINANCIAL_DATA (유저 재무 데이터)
create table user_financial_data
(
    value        int                                                                                            not null,
    collected_at datetime(6)                                                                                    not null,
    data_id      bigint auto_increment primary key,
    user_id      bigint                                                                                         not null,
    category     enum ('COMMUNICATION', 'EDUCATION', 'ETC', 'FOOD', 'HEALTH', 'LEISURE', 'LIVING', 'TRANSPORT') null,
    data_type    enum ('EXPENSE', 'INCOME', 'LOAN_BALANCE')                                                     not null,
    constraint FKqk7w04q7gt20twv8xi8y17865 foreign key (user_id) references member (member_id)
);

-- CONSUMPTION_HABIT_REPORT (소비 습관 리포트)
create table consumption_habit_report
(
    created_at   date         not null,
    report_month varchar(7)   not null,
    member_id    bigint       not null,
    report_id    bigint auto_increment primary key,
    summary      varchar(500) null,
    constraint UK1n794ss5uofb3j16hsnj3l21e unique (member_id, report_month),
    constraint FKmq81atoh4ght2iogu5ouictrn foreign key (member_id) references member (member_id)
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
    constraint FK5bcyk09s5o5ss2oag9kbcrvha foreign key (member_id) references member (member_id),
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
    constraint UK3hebrkl6ex5u6xv8wrj0m13mf unique (credential_id),
    constraint UKd3jsewxyq9sxygyaau5q46jcv unique (mfa_log_id),
    constraint UKnrnmxpttm9vs0jaowf9m5jr5g unique (member_id),
    constraint FK2q7688loi42jwjgvh8q5s1te3 foreign key (credential_id) references fido_credential (credential_id),
    constraint FK8u5ywo54pknmfyi542m7ou80 foreign key (mfa_log_id) references mfa_log (mfa_log_id),
    constraint FKt8w1awoivi0ilqtrwqrjwq2s2 foreign key (member_id) references member (member_id)
);