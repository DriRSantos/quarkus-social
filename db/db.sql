CREATE DATABASE quarkus-social;

CREATE TABLE users (
    id bigserial not null primary key,
    name varchar(100) not null,
    age integer not null
);

CREATE TABLE posts (
    id bigserial not null primary key,
    post_text varchar(150) not null,
    dateTime timestamp,
    user_id bigint not null references USERS(id)
);