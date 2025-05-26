-- Member 테이블 상에 phone 삭제 및 creditScoreEvaluated 추가
alter table member drop column phone;
alter table member add credit_score_evaluated;