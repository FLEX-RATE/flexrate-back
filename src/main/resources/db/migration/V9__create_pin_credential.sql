create table pin_credential (
    id bigint auto_increment primary key,
    member_id bigint not null,
    pin_hash varchar(255) not null,
    constraint fk_pin_credential_member foreign key (member_id) references member(member_id) on delete cascade
);