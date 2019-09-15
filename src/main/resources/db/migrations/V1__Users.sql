CREATE SCHEMA IF NOT EXISTS interactive;

SET search_path TO interactive;

CREATE FUNCTION set_update_date() RETURNS TRIGGER AS $set_update_date$
BEGIN
  NEW.updated := current_timestamp;
  RETURN NEW;
END;
$set_update_date$ LANGUAGE plpgsql;

CREATE TABLE users
(
    id CHARACTER VARYING(250) PRIMARY KEY,
    email CHARACTER VARYING(250) UNIQUE NOT NULL,
    first_name CHARACTER VARYING(50) NOT NULL,
    last_name CHARACTER VARYING(50) NOT NULL,
    birthday CHARACTER VARYING(50) NOT NULL,
    city CHARACTER VARYING(250) NOT NULL,
    phone CHARACTER VARYING(250) NOT NULL,
    occupation CHARACTER VARYING(250) NOT NULL,
    field CHARACTER VARYING(250) NOT NULL,
    english_level CHARACTER VARYING(250) NOT NULL,
    it_experience CHARACTER VARYING(250) NOT NULL,
    experience_description CHARACTER VARYING(250) NOT NULL,
    heard_from CHARACTER VARYING(250) NOT NULL,
    created TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER set_timestamp_users
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE PROCEDURE set_update_date();
