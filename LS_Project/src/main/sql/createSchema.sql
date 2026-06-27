drop table if exists tokens;
drop table if exists booking;
drop table if exists houses;
drop table if exists locations;
drop table if exists users;
drop type if exists location_type;

create type location_type as enum (
    'COUNTRY',
    'REGION',
    'DISTRICT',
    'MUNICIPALITY',
    'LOCALITY'
    );

create table users (
                       id serial primary key,
                       name varchar(80) not null,
                       email varchar(255) unique not null,
                       password varchar(255) not null
);

create table tokens (
                        token uuid primary key default gen_random_uuid(),
                        user_id integer not null references users(id) on delete cascade
);

create table locations (
                           id serial primary key,
                           name varchar(80) not null,
                           type location_type not null,
                           parent_id integer references locations(id) on delete cascade
);

create table houses (
                        id serial primary key,
                        owner_id integer not null references users(id) on delete restrict,
                        title varchar(80) not null,
                        location_id integer not null references locations(id) on delete restrict,
                        area_sqm decimal(10, 2) not null check (area_sqm > 0),
                        price_per_night decimal(10, 2) not null check (price_per_night > 0),
                        description text
);

create table booking (
                         id serial primary key,
                         user_id integer not null references users(id) on delete cascade,
                         house_id integer not null references houses(id) on delete cascade,
                         start_date DATE not null,
                         end_date DATE not null,
                         constraint check_dates check (end_date > start_date)
);
