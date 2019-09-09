CREATE DATABASE whiz;

\c whiz;

CREATE TABLE users
(
    user_id character varying(250) primary key,
    user_email character varying(250),
    user_first_name character varying(50),
    user_last_name character varying(50),
    user_password character varying(250),
    user_age int,
    user_address_id character varying(250),
    user_created timestamp,
    user_updated timestamp
);

