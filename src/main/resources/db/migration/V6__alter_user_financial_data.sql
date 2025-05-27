alter table user_financial_data
modify column category enum (
    'COMMUNICATION',
    'EDUCATION',
    'ETC',
    'FOOD',
    'HEALTH',
    'LEISURE',
    'LIVING',
    'TRANSPORT',
    'CLOTHING',
    'ELECTRONICS',
    'SUBSCRIPTION',
    'CAFE'
    ) null;