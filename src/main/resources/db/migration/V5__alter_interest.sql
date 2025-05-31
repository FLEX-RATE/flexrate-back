alter table interest
add column interest_changed bit not null,
modify column interest_date date not null;
