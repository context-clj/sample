-- chat
--$up
create table messages (
    id serial primary key,
    created_at timestamp default now(),
    author text not null,
    text text not null
);

--$
--$down
drop table messages;
--$