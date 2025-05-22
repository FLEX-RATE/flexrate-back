-- LOAN_REVIEW_HISTORY (대출 심사 이력)
create table loan_review_history
(
    review_id       bigint auto_increment primary key,
    application_id  bigint       not null,
    employment_type varchar(30)  null,
    annual_income   int          null,
    residence_type  varchar(30)  null,
    is_bankrupt     bit          null,
    loan_purpose    varchar(50)  null,
    constraint FK_loan_review_history_application foreign key (application_id) references loan_application (application_id)
);

-- LOAN_APPLICATION (대출 신청) 테이블에 대출 심사 이력 연관관계 추가
alter table loan_application
    add review_id bigint null,
    add constraint FK_loan_application_review foreign key (review_id) references loan_review_history (review_id);