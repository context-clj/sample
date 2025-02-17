-- init
--$up
create table patient (
  id serial primary key,
  ts timestamptz default current_timestamp,
  birthdate timestamptz,
  gender text,
  family text,
  given text
)
--$
--$down
drop table if exists patient;
--$
