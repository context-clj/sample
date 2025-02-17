-- notebooks
--$up
create table notebooks (
  id serial primary key,
  ts timestamptz default current_timestamp,
  title text not null,
  description text not null
);
--$
create table notebook_cells (
  id serial primary key,
  ts timestamptz default current_timestamp,
  updated_at timestamptz,
  position int not null,
  notebook int4 not null,
  type text,
  code text,
  params jsonb,
  data jsonb
);
--$down
drop table if exists notebook_cells;
--$
drop table if exists notebooks;
