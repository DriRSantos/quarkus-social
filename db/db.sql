CREATE TABLE quarkus-social;

CREATE TABLE users (
    id bigserial not null primary key,
    name varchar(100) not null,
    age integer not null
);