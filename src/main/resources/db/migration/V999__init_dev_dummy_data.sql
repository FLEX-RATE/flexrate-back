-- MEMBER (member 4, admin 1)
insert into member (birth_date, created_at, last_login_at, password_last_changed_at, updated_at, name, phone, email, password_hash, consume_goal, consumption_type, last_login_method, role, sex, status)
values
    ('1988-03-03', now(), now(), now(), now(), '관리자', '010-0000-0000', 'admin@email.com', '$2a$12$4HKfRhCR/uYPitv3qL6HcecOGNNv14D6ou56iLl54nCwcJoR69u.G', 'HAS_HOUSING_SAVING', 'PRACTICAL', 'PASSWORD', 'ADMIN', 'MALE', 'SUSPENDED'),
    ('1995-01-01', now(), now(), now(), now(), '홍길동', '010-1111-1111', 'member1@email.com', '$2a$10$576ZiPjvVCKQKUR8Zcpt0.sIQEwz8T8Ys4dKmAFSDGYOKjQvcx1fy', 'ONE_CATEGORY_SPEND', 'BALANCED', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),
    ('1992-02-02', now(), now(), now(), now(), '김영희', '010-2222-2222', 'member2@email.com', '$2a$10$w41NWOew7tkaU3lJUIA1YunUuTJbyXVXLBGE47iYgBBOiFs4ftxwO', 'NO_SPENDING_TODAY', 'CONSERVATIVE', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),
    ('2000-04-04', now(), now(), now(), now(), '박민정', '010-3333-3333', 'member3@email.com', '$2a$10$SOjqGxrZhaQT8Kg4.Eh3wOTLYPlcJhClJ4Dxm72O781zS.Dm.seF2', 'NO_EXPENSIVE_DESSERT', 'CONSUMPTION_ORIENTED', 'PASSWORD', 'MEMBER', 'FEMALE', 'WITHDRAWN'),
    ('1998-05-05', now(), now(), now(), now(), '이수진', '010-4444-4444', 'member4@email.com', '$2a$10$X.1bheS9oo7BJJbs6q0cTuGEdNnUsJhY.lR645Kj24oowXNN9xGA.', 'SMALL_MONTHLY_SAVE', 'BALANCED', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE');

-- LOAN_PRODUCT (대출 상품)
insert into loan_product (max_amount, max_rate, min_rate, terms, name, description)
values (10000000, 8.5, 3.5, 36, '플렉스레이트', '유연한 상환이 가능한 대표 대출 상품');

-- LOAN_APPLICATION (대출 신청)
insert into loan_application (credit_score, rate, remain_amount, total_amount, member_id, product_id, loan_type, status, applied_at, start_date, end_date, executed_at)
values
    (800, 5.0, 3000000, 5000000, 2, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 12 month), date_sub(now(), interval 12 month), date_add(now(), interval 24 month), date_sub(now(), interval 11 month)),
    (750, 4.5, 0, 3000000, 3, 1, 'EXTENSION', 'COMPLETED', date_sub(now(), interval 24 month), date_sub(now(), interval 24 month), date_sub(now(), interval 12 month), date_sub(now(), interval 23 month)),
    (720, 7.8, 1500000, 4000000, 4, 1, 'NEW', 'PENDING', date_sub(now(), interval 3 month), null, null, null),
    (810, 3.9, 2000000, 2000000, 5, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 1 month), date_sub(now(), interval 1 month), date_add(now(), interval 35 month), date_sub(now(), interval 1 month));

-- FIDO_CREDENTIAL
insert into fido_credential (is_active, sign_count, member_id, device_info, public_key, last_used_date)
values
    (1, 10, 2, 'Chrome-Win', 'pk1', date_sub(now(), interval 1 day)),
    (1, 5, 3, 'Safari-iOS', 'pk2', date_sub(now(), interval 3 day)),
    (0, 20, 4, 'Edge-Win', 'pk3', date_sub(now(), interval 10 day)),
    (1, 15, 5, 'Firefox-Mac', 'pk4', date_sub(now(), interval 5 day));

-- LOAN_TRANSACTION
insert into loan_transaction (amount, application_id, member_id, occurred_at, status, type)
values
    (2000000, 1, 2, date_sub(now(), interval 30 day), 'NORMAL', 'DELAY'),
    (500000, 1, 2, date_sub(now(), interval 25 day), 'NORMAL', 'REPAYMENT'),
    (1000000, 2, 3, date_sub(now(), interval 20 day), 'NORMAL', 'EXECUTION'),
    (300000, 2, 3, date_sub(now(), interval 15 day), 'NORMAL', 'REPAYMENT'),
    (1500000, 2, 3, date_sub(now(), interval 10 day), 'NORMAL', 'EXECUTION'),
    (2000000, 4, 5, date_sub(now(), interval 5 day), 'FAILED', 'EXECUTION');

-- MFA_LOG
insert into mfa_log (authenticated_at, mfa_type, result, device_info, transaction_id)
values
    (date_sub(now(), interval 10 day), 'SMS', 'SUCCESS', 'Galaxy S22', 1),
    (date_sub(now(), interval 8 day), 'EMAIL', 'FAILURE', 'iPhone 14', 2),
    (date_sub(now(), interval 6 day), 'PASS', 'SUCCESS', 'PC', 3),
    (date_sub(now(), interval 4 day), 'SMS', 'FAILURE', 'iPad', 4),
    (date_sub(now(), interval 2 day), 'EMAIL', 'SUCCESS', 'Macbook', 5);

-- INTEREST
insert into interest (interest_rate, application_id, interest_date)
values
    (5.0, 1, date_sub(now(), interval 5 day)),
    (4.9, 1, date_sub(now(), interval 4 day)),
    (4.8, 1, date_sub(now(), interval 3 day)),
    (4.7, 1, date_sub(now(), interval 2 day)),
    (4.6, 1, date_sub(now(), interval 1 day)),
    (4.5, 2, date_sub(now(), interval 5 day)),
    (4.4, 2, date_sub(now(), interval 4 day)),
    (4.3, 2, date_sub(now(), interval 3 day)),
    (4.2, 2, date_sub(now(), interval 2 day)),
    (4.1, 2, date_sub(now(), interval 1 day)),
    (4.0, 4, date_sub(now(), interval 5 day)),
    (3.9, 4, date_sub(now(), interval 4 day)),
    (3.8, 4, date_sub(now(), interval 3 day)),
    (3.7, 4, date_sub(now(), interval 2 day)),
    (3.6, 4, date_sub(now(), interval 1 day));


-- NOTIFICATION
insert into notification (is_read, member_id, sent_at, content, type)
values
    (0, 2, date_sub(now(), interval 5 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
    (1, 3, date_sub(now(), interval 4 day), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
    (0, 4, date_sub(now(), interval 3 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
    (1, 5, date_sub(now(), interval 2 day), '만기 알림입니다.', 'MATURITY_NOTICE');

-- REFRESH_TOKEN
insert into refresh_token (member_id, refresh_token)
values
    (1, 'refresh_token_1'),
    (2, 'refresh_token_2'),
    (3, 'refresh_token_3');

-- USER_FINANCIAL_DATA
insert into user_financial_data (value, collected_at, user_id, category, data_type)
values
    (30000, date_sub(now(), interval 1 day), 2, 'FOOD', 'EXPENSE'),
    (50000, date_sub(now(), interval 1 day), 2, 'TRANSPORT', 'EXPENSE'),
    (100000, date_sub(now(), interval 1 day), 2, 'COMMUNICATION', 'EXPENSE'),
    (20000, date_sub(now(), interval 1 day), 2, 'EDUCATION', 'EXPENSE'),
    (50000, date_sub(now(), interval 1 day), 2, 'HEALTH', 'EXPENSE'),
    (1000000, date_sub(now(), interval 1 month), 2, 'ETC', 'INCOME'),
    (50000, date_sub(now(), interval 1 day), 3, 'FOOD', 'EXPENSE'),
    (70000, date_sub(now(), interval 1 day), 3, 'TRANSPORT', 'EXPENSE'),
    (120000, date_sub(now(), interval 1 day), 3, 'COMMUNICATION', 'EXPENSE'),
    (30000, date_sub(now(), interval 1 day), 3, 'EDUCATION', 'EXPENSE'),
    (60000, date_sub(now(), interval 1 day), 3, 'HEALTH', 'EXPENSE'),
    (1500000, date_sub(now(), interval 1 month), 3, 'ETC', 'INCOME'),
    (20000, date_sub(now(), interval 1 day), 4, 'FOOD', 'EXPENSE'),
    (30000, date_sub(now(), interval 1 day), 4, 'TRANSPORT', 'EXPENSE'),
    (50000, date_sub(now(), interval 1 day), 4, 'COMMUNICATION', 'EXPENSE'),
    (10000, date_sub(now(), interval 1 day), 4, 'EDUCATION', 'EXPENSE'),
    (20000, date_sub(now(), interval 1 day), 4, 'HEALTH', 'EXPENSE'),
    (500000, date_sub(now(), interval 1 month), 4, 'ETC', 'INCOME'),
    (100000, date_sub(now(), interval 1 day), 5, 'FOOD', 'EXPENSE'),
    (150000, date_sub(now(), interval 1 day), 5, 'TRANSPORT', 'EXPENSE'),
    (250000, date_sub(now(), interval 1 day), 5, 'COMMUNICATION', 'EXPENSE'),
    (50000, date_sub(now(), interval 1 day), 5, 'EDUCATION', 'EXPENSE'),
    (100000, date_sub(now(), interval 1 day), 5, 'HEALTH', 'EXPENSE'),
    (150000, date_sub(now(), interval 1 day), 3, 'LEISURE', 'EXPENSE'),
    (250000, date_sub(now(), interval 1 day), 4, 'EDUCATION', 'EXPENSE'),
    (120000, date_sub(now(), interval 1 day), 5, 'COMMUNICATION', 'EXPENSE'),
    (2000000, date_sub(now(), interval 1 month), 2, 'ETC', 'INCOME');

-- CONSUMPTION_HABIT_REPORT
insert into consumption_habit_report (created_at, report_month, member_id, summary)
values
    (date_sub(now(), interval 7 day), '2024-05', 1, '지출이 꾸준히 감소했습니다.'),
    (date_sub(now(), interval 37 day), '2024-04', 2, '소비 패턴이 안정적입니다.'),
    (date_sub(now(), interval 67 day), '2024-03', 3, '저축 비율이 높아졌습니다.');

-- AUDIT_LOG
insert into audit_log (auth_method, event_type, member_id, occurred_at, ip_address, device_info, action, detail)
values
    (0, 1, 2, now(), '192.168.0.1', 'Chrome-Win', '로그인', '정상 로그인'),
    (1, 2, 3, now(), '192.168.0.2', 'Safari-iOS', '비밀번호 변경', '비밀번호 변경 성공'),
    (0, 0, 4, now(), '192.168.0.3', 'Edge-Win', '로그아웃', '정상 로그아웃');

-- AUTHENTICATION
insert into authentication (authenticated_at, credential_id, member_id, mfa_log_id, ip_address, device_info, auth_method)
values
    (now(), 1, 1, 1, '192.168.0.1', 'Chrome-Win', 'FIDO'),
    (now(), 2, 2, 2, '192.168.0.2', 'Safari-iOS', 'FIDO'),
    (now(), 3, 3, 3, '192.168.0.3', 'Edge-Win', 'FIDO');