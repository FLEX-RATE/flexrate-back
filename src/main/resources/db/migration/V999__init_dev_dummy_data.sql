-- MEMBER (member 33, admin 1)
insert into member(member_id, age, birth_date, created_at, last_login_at, password_last_changed_at, updated_at, name, phone, email, password_hash, consume_goal, consumption_type, last_login_method, role, sex, status)
values
    -- ADMIN
    (1, null, '1988-03-03', now(6), now(6), now(6), now(6), '관리자', '010-0000-0000', 'admin@email.com', '$2a$12$BBE6HrevyW5y6tMmgoLmyes2D.jIHdjeFK99YQ3ozo8gyZ9LLbozy', 'ONLY_PUBLIC_TRANSPORT', 'PRACTICAL', 'PASSWORD', 'ADMIN', 'MALE', 'SUSPENDED'),

    -- CONSERVATIVE (4)
    (2, null, '1992-02-02', now(6), now(6), now(6), now(6), '김영희', '010-2222-2222', 'member2@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_SPENDING_TODAY', 'CONSERVATIVE', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (3, null, '1990-01-10', now(6), now(6), now(6), now(6), '최성민', '010-5555-5555', 'member5@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'LIMIT_DAILY_MEAL', 'CONSERVATIVE', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),
    (4, null, '1987-07-17', now(6), now(6), now(6), now(6), '박서진', '010-6666-6666', 'member6@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'SAVE_70_PERCENT', 'CONSERVATIVE', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (5, null, '1985-12-05', now(6), now(6), now(6), now(6), '이진우', '010-7777-7777', 'member7@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'INCOME_OVER_EXPENSE', 'CONSERVATIVE', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),

    -- PRACTICAL (4)
    (6, null, '1989-08-08', now(6), now(6), now(6), now(6), '정하늘', '010-8888-8888', 'member8@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'COMPARE_BEFORE_BUYING', 'PRACTICAL', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (7, null, '1991-09-09', now(6), now(6), now(6), now(6), '윤지훈', '010-9999-9999', 'member9@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'HAS_HOUSING_SAVING', 'PRACTICAL', 'SOCIAL', 'MEMBER', 'MALE', 'ACTIVE'),
    (8, null, '1993-03-03', now(6), now(6), now(6), now(6), '한유진', '010-1010-1010', 'member10@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'CLOTHING_UNDER_100K', 'PRACTICAL', 'PASSWORD', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (9, null, '1986-06-06', now(6), now(6), now(6), now(6), '김태현', '010-1212-1212', 'member11@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'ONLY_PUBLIC_TRANSPORT', 'PRACTICAL', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),

    -- BALANCED (4)
    (10, null, '1995-01-01', now(6), now(6), now(6), now(6), '홍길동', '010-1111-1111', 'member1@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'ONE_CATEGORY_SPEND', 'BALANCED', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),
    (11, null, '1998-05-05', now(6), now(6), now(6), now(6), '이수진', '010-4444-4444', 'member4@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'SMALL_MONTHLY_SAVE', 'BALANCED', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (12, null, '1994-04-14', now(6), now(6), now(6), now(6), '서지호', '010-1313-1313', 'member12@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_USELESS_ELECTRONICS', 'BALANCED', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),
    (13, null, '1996-06-16', now(6), now(6), now(6), now(6), '조민아', '010-1414-1414', 'member13@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'OVER_10_PERCENT', 'BALANCED', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),

    -- CONSUMPTION_ORIENTED (4)
    (14, null, '2000-04-04', now(6), now(6), now(6), now(6), '박민정', '010-3333-3333', 'member3@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_EXPENSIVE_DESSERT', 'CONSUMPTION_ORIENTED', 'PASSWORD', 'MEMBER', 'FEMALE', 'WITHDRAWN'),
    (15, null, '1997-07-07', now(6), now(6), now(6), now(6), '신동욱', '010-1515-1515', 'member14@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_OVER_50K_PER_DAY', 'CONSUMPTION_ORIENTED', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),
    (16, null, '1999-09-09', now(6), now(6), now(6), now(6), '문지수', '010-1616-1616', 'member15@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'SUBSCRIPTION_UNDER_50K', 'CONSUMPTION_ORIENTED', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (17, null, '1993-12-12', now(6), now(6), now(6), now(6), '임채원', '010-1717-1717', 'member16@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'MEAL_UNDER_20K', 'CONSUMPTION_ORIENTED', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),

    -- 나머지 멤버(랜덤 배치, 총 33명 맞추기 위해 17명 추가)
    (18, null, '1990-02-10', now(6), now(6), now(6), now(6), '김도현', '010-1818-1818', 'member17@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_SPENDING_TODAY', 'CONSERVATIVE', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),
    (19, null, '1991-03-15', now(6), now(6), now(6), now(6), '이유림', '010-1919-1919', 'member18@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'LIMIT_DAILY_MEAL', 'CONSERVATIVE', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (20, null, '1992-04-20', now(6), now(6), now(6), now(6), '박은서', '010-2020-2020', 'member19@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'SAVE_70_PERCENT', 'CONSERVATIVE', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),
    (21, null, '1993-05-25', now(6), now(6), now(6), now(6), '최승훈', '010-2121-2121', 'member20@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'INCOME_OVER_EXPENSE', 'CONSERVATIVE', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (22, null, '1994-06-30', now(6), now(6), now(6), now(6), '정다은', '010-2222-2323', 'member21@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'COMPARE_BEFORE_BUYING', 'PRACTICAL', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),
    (23, null, '1995-07-05', now(6), now(6), now(6), now(6), '한수빈', '010-2424-2424', 'member22@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'HAS_HOUSING_SAVING', 'PRACTICAL', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (24, null, '1996-08-10', now(6), now(6), now(6), now(6), '서지훈', '010-2525-2525', 'member23@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'CLOTHING_UNDER_100K', 'PRACTICAL', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),
    (25, null, '1997-09-15', now(6), now(6), now(6), now(6), '문예린', '010-2626-2626', 'member24@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'ONLY_PUBLIC_TRANSPORT', 'PRACTICAL', 'PASSWORD', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (26, null, '1998-10-20', now(6), now(6), now(6), now(6), '신유진', '010-2727-2727', 'member25@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'ONE_CATEGORY_SPEND', 'BALANCED', 'SOCIAL', 'MEMBER', 'MALE', 'ACTIVE'),
    (27, null, '1999-11-25', now(6), now(6), now(6), now(6), '오지혁', '010-2828-2828', 'member26@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'SMALL_MONTHLY_SAVE', 'BALANCED', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (28, null, '2000-12-30', now(6), now(6), now(6), now(6), '이서준', '010-2929-2929', 'member27@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_USELESS_ELECTRONICS', 'BALANCED', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE'),
    (29, null, '2001-01-04', now(6), now(6), now(6), now(6), '최지아', '010-3030-3030', 'member28@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'OVER_10_PERCENT', 'BALANCED', 'SOCIAL', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (30, null, '2002-02-08', now(6), now(6), now(6), now(6), '임도윤', '010-3131-3131', 'member29@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_EXPENSIVE_DESSERT', 'CONSUMPTION_ORIENTED', 'PASSKEY', 'MEMBER', 'MALE', 'ACTIVE'),
    (31, null, '2003-03-12', now(6), now(6), now(6), now(6), '강지우', '010-3232-3232', 'member30@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_OVER_50K_PER_DAY', 'CONSUMPTION_ORIENTED', 'PASSWORD', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (32, null, '2004-04-16', now(6), now(6), now(6), now(6), '서하윤', '010-3333-3333', 'member31@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'SUBSCRIPTION_UNDER_50K', 'CONSUMPTION_ORIENTED', 'SOCIAL', 'MEMBER', 'MALE', 'ACTIVE'),
    (33, null, '2005-05-20', now(6), now(6), now(6), now(6), '박시윤', '010-3434-3434', 'member32@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'MEAL_UNDER_20K', 'CONSUMPTION_ORIENTED', 'PASSKEY', 'MEMBER', 'FEMALE', 'ACTIVE'),
    (34, null, '2006-06-24', now(6), now(6), now(6), now(6), '조하람', '010-3535-3535', 'member33@email.com', '$2a$12$B2ogNikMQCOZ5Ulapyxtn.5Z3i323vYWRlHLspgx/aZgJPrQGmeT2', 'NO_SPENDING_TODAY', 'CONSERVATIVE', 'PASSWORD', 'MEMBER', 'MALE', 'ACTIVE');


-- LOAN_PRODUCT (대출 상품)
insert into loan_product (max_amount, max_rate, min_rate, terms, name, description)
values (10000000, 8.5, 3.5, 36, '플렉스레이트', '당신의 라이프스타일로 평가받는 신용대출');

-- LOAN_APPLICATION (대출 신청)
insert into loan_application
(credit_score, rate, remain_amount, total_amount, member_id, product_id, loan_type, status, applied_at, start_date, end_date, executed_at)
values
    (800, 5.0, 3000000, 5000000, 2, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 12 month), date_sub(now(), interval 12 month), date_add(now(), interval 24 month), date_sub(now(), interval 11 month)),
    (750, 4.5, 0, 3000000, 3, 1, 'EXTENSION', 'COMPLETED', date_sub(now(), interval 24 month), date_sub(now(), interval 24 month), date_sub(now(), interval 12 month), date_sub(now(), interval 23 month)),
    (720, 7.8, 4000000, 4000000, 4, 1, 'NEW', 'PENDING', date_sub(now(), interval 3 month), null, null, null),
    (810, 3.9, 2000000, 2000000, 5, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 1 month), date_sub(now(), interval 1 month), date_add(now(), interval 35 month), date_sub(now(), interval 1 month)),
    (690, 8.2, 2000000, 2000000, 6, 1, 'NEW', 'PENDING', date_sub(now(), interval 2 month), null, null, null),
    (710, 6.5, 0, 1500000, 7, 1, 'REJOIN', 'REJECTED', date_sub(now(), interval 6 month), null, null, null),
    (730, 4.7, 1200000, 3000000, 8, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 8 month), date_sub(now(), interval 8 month), date_add(now(), interval 28 month), date_sub(now(), interval 7 month)),
    (820, 3.7, 0, 1000000, 9, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 36 month), date_sub(now(), interval 36 month), date_sub(now(), interval 0 month), date_sub(now(), interval 35 month)),
    (735, 5.2, 800000, 2000000, 10, 1, 'EXTENSION', 'EXECUTED', date_sub(now(), interval 10 month), date_sub(now(), interval 10 month), date_add(now(), interval 26 month), date_sub(now(), interval 9 month)),
    (700, 7.4, 0, 2500000, 11, 1, 'NEW', 'REJECTED', date_sub(now(), interval 5 month), null, null, null),
    (780, 4.1, 400000, 1000000, 12, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 4 month), date_sub(now(), interval 4 month), date_add(now(), interval 32 month), date_sub(now(), interval 3 month)),
    (765, 6.8, 0, 2200000, 13, 1, 'REJOIN', 'COMPLETED', date_sub(now(), interval 20 month), date_sub(now(), interval 20 month), date_sub(now(), interval 8 month), date_sub(now(), interval 19 month)),
    (805, 3.5, 0, 500000, 14, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 13 month), date_sub(now(), interval 13 month), date_add(now(), interval 23 month), date_sub(now(), interval 12 month)),
    (720, 7.1, 3000000, 3000000, 15, 1, 'NEW', 'PRE_APPLIED', date_sub(now(), interval 1 month), null, null, null),
    (750, 5.8, 0, 1800000, 16, 1, 'NEW', 'REJECTED', date_sub(now(), interval 7 month), null, null, null),
    (790, 4.2, 600000, 1200000, 17, 1, 'EXTENSION', 'EXECUTED', date_sub(now(), interval 15 month), date_sub(now(), interval 15 month), date_add(now(), interval 21 month), date_sub(now(), interval 14 month)),
    (830, 3.8, 0, 700000, 18, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 18 month), date_sub(now(), interval 18 month), date_add(now(), interval 18 month), date_sub(now(), interval 17 month)),
    (700, 8.0, 1300000, 3000000, 19, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 9 month), date_sub(now(), interval 9 month), date_add(now(), interval 27 month), date_sub(now(), interval 8 month)),
    (695, 7.6, 0, 1200000, 20, 1, 'REJOIN', 'REJECTED', date_sub(now(), interval 11 month), null, null, null),
    (780, 4.9, 900000, 2000000, 21, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 6 month), date_sub(now(), interval 6 month), date_add(now(), interval 30 month), date_sub(now(), interval 5 month)),
    (760, 5.5, 0, 2500000, 22, 1, 'NEW', 'PENDING', date_sub(now(), interval 2 month), null, null, null),
    (720, 7.0, 1100000, 2000000, 23, 1, 'EXTENSION', 'EXECUTED', date_sub(now(), interval 17 month), date_sub(now(), interval 17 month), date_add(now(), interval 19 month), date_sub(now(), interval 16 month)),
    (815, 3.6, 0, 600000, 24, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 22 month), date_sub(now(), interval 22 month), date_add(now(), interval 14 month), date_sub(now(), interval 21 month)),
    (740, 6.2, 100000, 1000000, 25, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 3 month), date_sub(now(), interval 3 month), date_add(now(), interval 33 month), date_sub(now(), interval 2 month)),
    (700, 8.1, 0, 900000, 26, 1, 'REJOIN', 'REJECTED', date_sub(now(), interval 14 month), null, null, null),
    (785, 4.6, 500000, 1500000, 27, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 7 month), date_sub(now(), interval 7 month), date_add(now(), interval 29 month), date_sub(now(), interval 6 month)),
    (810, 3.9, 0, 800000, 28, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 25 month), date_sub(now(), interval 25 month), date_add(now(), interval 11 month), date_sub(now(), interval 24 month)),
    (730, 6.9, 200000, 1200000, 29, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 5 month), date_sub(now(), interval 5 month), date_add(now(), interval 31 month), date_sub(now(), interval 4 month)),
    (715, 7.3, 0, 1400000, 30, 1, 'EXTENSION', 'REJECTED', date_sub(now(), interval 16 month), null, null, null),
    (805, 3.7, 0, 500000, 31, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 19 month), date_sub(now(), interval 19 month), date_add(now(), interval 17 month), date_sub(now(), interval 18 month)),
    (690, 8.5, 1500000, 3500000, 32, 1, 'NEW', 'EXECUTED', date_sub(now(), interval 2 month), date_sub(now(), interval 2 month), date_add(now(), interval 34 month), date_sub(now(), interval 1 month)),
    (750, 5.9, 0, 2000000, 33, 1, 'REJOIN', 'PENDING', date_sub(now(), interval 1 month), null, null, null),
    (820, 3.6, 0, 650000, 34, 1, 'NEW', 'COMPLETED', date_sub(now(), interval 28 month), date_sub(now(), interval 28 month), date_add(now(), interval 8 month), date_sub(now(), interval 27 month));

-- LOAN_TRANSACTION
insert into loan_transaction (amount, application_id, member_id, occurred_at, status, type)
values
    -- application_id: 1, member_id: 2, 집행/상환/연체
    (5000000, 1, 2, date_sub(now(), interval 11 month), 'NORMAL', 'EXECUTION'),
    (1000000, 1, 2, date_sub(now(), interval 9 month), 'NORMAL', 'REPAYMENT'),
    (1000000, 1, 2, date_sub(now(), interval 6 month), 'NORMAL', 'REPAYMENT'),
    (1000000, 1, 2, date_sub(now(), interval 3 month), 'FAILED', 'DELAY'),

    -- application_id: 4, member_id: 5, 집행/상환
    (2000000, 4, 5, date_sub(now(), interval 1 month), 'NORMAL', 'EXECUTION'),
    (500000, 4, 5, date_sub(now(), interval 15 day), 'NORMAL', 'REPAYMENT'),

    -- application_id: 7, member_id: 8, 집행/상환/연체
    (3000000, 7, 8, date_sub(now(), interval 7 month), 'NORMAL', 'EXECUTION'),
    (900000, 7, 8, date_sub(now(), interval 5 month), 'NORMAL', 'REPAYMENT'),
    (900000, 7, 8, date_sub(now(), interval 2 month), 'FAILED', 'DELAY'),

    -- application_id: 12, member_id: 17, 집행/상환
    (1200000, 12, 17, date_sub(now(), interval 14 month), 'NORMAL', 'EXECUTION'),
    (300000, 12, 17, date_sub(now(), interval 11 month), 'NORMAL', 'REPAYMENT'),

    -- application_id: 16, member_id: 21, 집행/상환
    (2000000, 16, 21, date_sub(now(), interval 5 month), 'NORMAL', 'EXECUTION'),
    (700000, 16, 21, date_sub(now(), interval 3 month), 'NORMAL', 'REPAYMENT'),

    -- application_id: 17, member_id: 19, 집행/상환/연체
    (3000000, 17, 19, date_sub(now(), interval 8 month), 'NORMAL', 'EXECUTION'),
    (1000000, 17, 19, date_sub(now(), interval 5 month), 'NORMAL', 'REPAYMENT'),
    (700000, 17, 19, date_sub(now(), interval 2 month), 'FAILED', 'DELAY'),

    -- application_id: 20, member_id: 23, 집행/상환
    (2000000, 20, 23, date_sub(now(), interval 16 month), 'NORMAL', 'EXECUTION'),
    (500000, 20, 23, date_sub(now(), interval 13 month), 'NORMAL', 'REPAYMENT'),

    -- application_id: 22, member_id: 25, 집행/상환/연체
    (1700000, 22, 34, date_sub(now(), interval 12 month), 'NORMAL', 'EXECUTION'),
    (400000, 22, 34, date_sub(now(), interval 10 month), 'NORMAL', 'REPAYMENT'),
    (300000, 22, 34, date_sub(now(), interval 2 month), 'FAILED', 'DELAY');

-- INTEREST
insert into interest (interest_rate, application_id, interest_date)
values
-- application_id: 1 (5.0% → 점진적 하락)
(5.000, 1, date_sub(now(), interval 4 day)),
(4.995, 1, date_sub(now(), interval 3 day)),
(4.990, 1, date_sub(now(), interval 2 day)),
(4.988, 1, date_sub(now(), interval 1 day)),
(4.985, 1, now()),

-- application_id: 4 (3.9% → 소폭 등락, 전체적 하락)
(3.900, 4, date_sub(now(), interval 4 day)),
(3.902, 4, date_sub(now(), interval 3 day)),
(3.899, 4, date_sub(now(), interval 2 day)),
(3.894, 4, date_sub(now(), interval 1 day)),
(3.891, 4, now()),

-- application_id: 7 (4.7% → 점진적 하락)
(4.700, 7, date_sub(now(), interval 4 day)),
(4.695, 7, date_sub(now(), interval 3 day)),
(4.690, 7, date_sub(now(), interval 2 day)),
(4.688, 7, date_sub(now(), interval 1 day)),
(4.685, 7, now()),

-- application_id: 10 (5.2% → 점진적 하락)
(5.200, 10, date_sub(now(), interval 4 day)),
(5.198, 10, date_sub(now(), interval 3 day)),
(5.195, 10, date_sub(now(), interval 2 day)),
(5.192, 10, date_sub(now(), interval 1 day)),
(5.190, 10, now()),

-- application_id: 12 (4.1% → 점진적 하락)
(4.100, 12, date_sub(now(), interval 4 day)),
(4.098, 12, date_sub(now(), interval 3 day)),
(4.096, 12, date_sub(now(), interval 2 day)),
(4.093, 12, date_sub(now(), interval 1 day)),
(4.090, 12, now()),

-- application_id: 17 (4.2% → 소폭 등락, 전체적 하락)
(4.200, 17, date_sub(now(), interval 4 day)),
(4.202, 17, date_sub(now(), interval 3 day)), -- 소폭 상승
(4.198, 17, date_sub(now(), interval 2 day)),
(4.194, 17, date_sub(now(), interval 1 day)),
(4.191, 17, now()),

-- application_id: 19 (8.0% → 점진적 하락)
(8.000, 19, date_sub(now(), interval 4 day)),
(7.995, 19, date_sub(now(), interval 3 day)),
(7.990, 19, date_sub(now(), interval 2 day)),
(7.988, 19, date_sub(now(), interval 1 day)),
(7.985, 19, now()),

-- application_id: 21 (4.9% → 점진적 하락)
(4.900, 21, date_sub(now(), interval 4 day)),
(4.897, 21, date_sub(now(), interval 3 day)),
(4.895, 21, date_sub(now(), interval 2 day)),
(4.892, 21, date_sub(now(), interval 1 day)),
(4.890, 21, now()),

-- application_id: 23 (7.0% → 점진적 하락)
(7.000, 23, date_sub(now(), interval 4 day)),
(6.995, 23, date_sub(now(), interval 3 day)),
(6.990, 23, date_sub(now(), interval 2 day)),
(6.988, 23, date_sub(now(), interval 1 day)),
(6.985, 23, now()),

-- application_id: 25 (6.2% → 소폭 등락, 전체적 하락)
(6.200, 25, date_sub(now(), interval 4 day)),
(6.202, 25, date_sub(now(), interval 3 day)), -- 소폭 상승
(6.199, 25, date_sub(now(), interval 2 day)),
(6.194, 25, date_sub(now(), interval 1 day)),
(6.191, 25, now()),

-- application_id: 27 (4.6% → 점진적 하락)
(4.600, 27, date_sub(now(), interval 4 day)),
(4.595, 27, date_sub(now(), interval 3 day)),
(4.590, 27, date_sub(now(), interval 2 day)),
(4.588, 27, date_sub(now(), interval 1 day)),
(4.585, 27, now()),

-- application_id: 29 (6.9% → 점진적 하락)
(6.900, 29, date_sub(now(), interval 4 day)),
(6.895, 29, date_sub(now(), interval 3 day)),
(6.890, 29, date_sub(now(), interval 2 day)),
(6.888, 29, date_sub(now(), interval 1 day)),
(6.885, 29, now());

-- NOTIFICATION
insert into notification (is_read, member_id, sent_at, content, type)
values
-- 대출 승인 알림
(0, 2, date_sub(now(), interval 12 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(1, 5, date_sub(now(), interval 1 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 8, date_sub(now(), interval 8 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 10, date_sub(now(), interval 10 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(1, 17, date_sub(now(), interval 4 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 19, date_sub(now(), interval 15 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 21, date_sub(now(), interval 9 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(1, 23, date_sub(now(), interval 6 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 25, date_sub(now(), interval 17 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(1, 27, date_sub(now(), interval 7 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 29, date_sub(now(), interval 5 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(0, 32, date_sub(now(), interval 2 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),
(1, 34, date_sub(now(), interval 13 month), '대출이 승인되었습니다.', 'LOAN_APPROVAL'),

-- 금리 변동 알림
(0, 2, date_sub(now(), interval 2 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(1, 5, date_sub(now(), interval 1 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 8, date_sub(now(), interval 3 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 10, date_sub(now(), interval 2 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 17, date_sub(now(), interval 4 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(1, 19, date_sub(now(), interval 1 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 21, date_sub(now(), interval 2 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 23, date_sub(now(), interval 3 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(1, 25, date_sub(now(), interval 1 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 27, date_sub(now(), interval 3 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(1, 29, date_sub(now(), interval 1 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),
(0, 32, date_sub(now(), interval 2 day), '금리가 0.1% 낮아졌습니다.', 'INTEREST_RATE_CHANGE'),

-- 만기 알림 (start_date + 11개월, 만기 7일 전)
(0, 2, date_add(date_sub(now(), interval 12 month), interval 11 month), '대출 만기 7일 전입니다.', 'MATURITY_NOTICE'),
(1, 5, date_add(date_sub(now(), interval 1 month), interval 11 month), '대출 만기 7일 전입니다.', 'MATURITY_NOTICE');

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
    (date_sub(now(), interval 7 day), '2024-05', 1, '최근 소비 내용을 살펴보면, 꼭 필요한 생활비와 자기계발에 균형 있게 지출하고 계신 모습이 인상적이에요. 특히 자기계발에 투자하는 비율이 높아서, 단순한 소비보다는 앞으로를 위한 사용에 가치를 두고 계시다는 느낌이 들어요! 다만 가끔 외식이나 취미 쪽에서 예산보다 조금 더 쓰는 경향이 보이는데요, 이런 부분은 월초에 ‘자유롭게 써도 되는 예산’을 따로 정해두면 마음도 편하고 소비 후 아쉬움도 덜 수 있을 것 같아요. 지금처럼 균형 있는 소비를 유지하신다면 앞으로도 큰 걱정 없이 잘 관리해나가실 수 있을 거예요!'),
    (date_sub(now(), interval 37 day), '2024-04', 2, '교통비와 식비 비중이 높게 나타났어요. 이동이 잦았던 만큼, 평소보다 교통비가 많이 들었네요.'),
    (date_sub(now(), interval 67 day), '2024-03', 3, '건강 관리에 대한 지출이 꾸준히 유지되고 있습니다. 식비와 여가비도 적절하게 분배되어 있어요.');

-- report_id = 1 (2024-05, member_id=1)
insert into consumption_habit_category (report_id, category, amount, ratio) values
    (1, 'FOOD', 90000, 30.00),
    (1, 'EDUCATION', 80000, 26.67),
    (1, 'LEISURE', 60000, 20.00),
    (1, 'COMMUNICATION', 30000, 10.00),
    (1, 'TRANSPORT', 20000, 6.67),
    (1, 'HEALTH', 10000, 3.33),
    (1, 'ETC', 5000, 1.67),
    (1, 'LIVING', 5000, 1.66);

-- report_id = 2 (2024-04, member_id=2)
insert into consumption_habit_category (report_id, category, amount, ratio) values
     (2, 'TRANSPORT', 60000, 30.00),
     (2, 'FOOD', 55000, 27.50),
     (2, 'LIVING', 30000, 15.00),
     (2, 'COMMUNICATION', 20000, 10.00),
     (2, 'LEISURE', 15000, 7.50),
     (2, 'HEALTH', 10000, 5.00),
     (2, 'ETC', 5000, 2.50),
     (2, 'EDUCATION', 5000, 2.50);

-- report_id = 3 (2024-03, member_id=3)
insert into consumption_habit_category (report_id, category, amount, ratio) values
     (3, 'HEALTH', 50000, 25.00),
     (3, 'FOOD', 50000, 25.00),
     (3, 'LEISURE', 40000, 20.00),
     (3, 'LIVING', 20000, 10.00),
     (3, 'COMMUNICATION', 15000, 7.50),
     (3, 'TRANSPORT', 10000, 5.00),
     (3, 'EDUCATION', 7500, 3.75),
     (3, 'ETC', 2500, 1.25);

-- AUDIT_LOG
insert into audit_log (auth_method, event_type, member_id, occurred_at, ip_address, device_info, action, detail)
values
    (0, 1, 2, now(), '192.168.0.1', 'Chrome-Win', '로그인', '정상 로그인'),
    (1, 2, 3, now(), '192.168.0.2', 'Safari-iOS', '비밀번호 변경', '비밀번호 변경 성공'),
    (0, 0, 4, now(), '192.168.0.3', 'Edge-Win', '로그아웃', '정상 로그아웃');
