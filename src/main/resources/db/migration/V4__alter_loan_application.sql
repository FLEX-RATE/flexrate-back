-- loan_application(대출 신청) 테이블 status 필드 타입 변경
alter table loan_application
modify status enum ('NONE', 'PRE_APPLIED', 'PENDING', 'REJECTED', 'EXECUTED', 'COMPLETED') not null;

-- loan_review_history(대출 심사 이력) 테이블 emplyment_type, residence_type, loan_purpose 필드 타입 변경
alter table loan_review_history
modify column employment_type enum ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'SELF_EMPLOYED', 'UNEMPLOYED', 'ETC') null,
modify column residence_type enum('OWN', 'JEONSE', 'MONTHLY', 'NO_HOUSE') null,
modify column loan_purpose enum('LIVING', 'BUSINESS', 'CAR', 'EDUCATION', 'HOUSE', 'ETC') null;