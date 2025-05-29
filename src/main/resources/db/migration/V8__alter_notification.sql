alter table notification
    modify column type enum(
        'LOAN_APPROVAL',
        'LOAN_REJECTED',
        'INTEREST_RATE_CHANGE',
        'MATURITY_NOTICE'
        ) not null;