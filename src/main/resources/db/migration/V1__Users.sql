create function set_update_date() returns trigger as $set_update_date$
begin
  if new.updated is null then
    new.updated := current_timestamp;
  end if;
  return new;
end;
$set_update_date$ language plpgsql;

create table english_level_c(
    id serial primary key,
    value varchar not null unique,
    label varchar
);

insert into english_level_c(value,label) values('Beginner','Beginner');
insert into english_level_c(value,label) values('Average','Average');
insert into english_level_c(value,label) values('Advanced','Advanced');

create table occupation_c(
  id serial primary key,
  value varchar not null unique,
  label varchar
);

insert into occupation_c(value,label) values('Employee','Employee');
insert into occupation_c(value,label) values('Student','Student');
insert into occupation_c(value,label) values('SelfEmployed','SelfEmployed');
insert into occupation_c(value,label) values('NoOccupation','NoOccupation');

create table field_of_work_c(
  id serial primary key,
  value varchar not null unique,
  label varchar
);

insert into field_of_work_c(value,label) values('Business/Management','Business/Management');
insert into field_of_work_c(value,label) values( 'Customer Support/Call Center','Customer Support/Call Center');
insert into field_of_work_c(value,label) values('Law','Law');
insert into field_of_work_c(value,label) values('Education/Training','Education/Training');
insert into field_of_work_c(value,label) values('Finance/Banks','Finance/Banks');
insert into field_of_work_c(value,label) values('HR/Human Resources','HR/Human Resources');
insert into field_of_work_c(value,label) values('It/Engineering/Technical', 'It/Engineering/Technical');
insert into field_of_work_c(value,label) values('Logistics/Transportation', 'Logistics/Transportation');
insert into field_of_work_c(value,label) values('Marketing/Advertising/PR', 'Marketing/Advertising/PR');
insert into field_of_work_c(value,label) values('Medical/Health','Medical/Health');
insert into field_of_work_c(value,label) values('Production','Production');
insert into field_of_work_c(value,label) values('Services/Sales','Services/Sales');
insert into field_of_work_c(value,label) values('Another','Another');

create table users (
  id varchar primary key,
  email varchar unique not null,
  first_name varchar not null,
  last_name varchar not null,
  birthday timestamptz not null,
  city varchar not null,
  phone varchar not null,
  occupation smallint not null references occupation_c(id),
  field_of_work smallint not null references field_of_work_c(id),
  english_level smallint not null references english_level_c(id),
  it_experience boolean not null,
  experience_description text,
  heard_from varchar not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_users
before update on users for each row execute procedure set_update_date();

create table groups (
  id varchar primary key,
  description varchar,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_groups
before update on groups for each row execute procedure set_update_date();

insert into groups(id,description) values('student','student');
insert into groups(id,description) values('admin','admin');

create table groups_users(
  group_id varchar not null references groups(id),
  user_id varchar not null references users(id), 
  created timestamptz not null default now(),
  updated timestamptz not null default now(),
  primary key(user_id, group_id)
);

create trigger set_timestamp_groups_users
before update on groups_users for each row execute procedure set_update_date();

CREATE TABLE invitations (
  id VARCHAR NOT NULL PRIMARY KEY,
  user_id VARCHAR NOT NULL REFERENCES users(id),
  expires_at TIMESTAMP NOT NULL,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_invitations
before update on invitations for each row execute procedure set_update_date();

CREATE TABLE quizzes (
  id VARCHAR NOT NULL PRIMARY KEY,
  user_id VARCHAR NOT NULL REFERENCES users(id),
  description TEXT NOT NULL, 
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_quizzes
before update on quizzes for each row execute procedure set_update_date();

CREATE TABLE question_sets (
  id VARCHAR NOT NULL PRIMARY KEY,
  quizz_id VARCHAR NOT NULL REFERENCES quizzes(id),
  title TEXT NOT NULL, 
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_question_sets
before update on question_sets for each row execute procedure set_update_date();

CREATE TABLE questions (
  id VARCHAR NOT NULL PRIMARY KEY,
  question_set_id VARCHAR REFERENCES question_sets(id),
  content TEXT NOT NULL, 
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_questions
before update on questions for each row execute procedure set_update_date();

CREATE TABLE answers (
  id VARCHAR NOT NULL PRIMARY KEY,
  question_id VARCHAR NOT NULL REFERENCES questions(id),
  content TEXT NOT NULL, 
  is_correct BOOLEAN,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_answers
before update on answers for each row execute procedure set_update_date();

insert into users(id, email, first_name, last_name, birthday, city, phone, occupation, 
  field_of_work, english_level, it_experience, experience_description, heard_from) values (
  'user0', 'user0@example.com','John','User', '1980-01-30 00:00:00Z', 
  'Iasi','0740012345', 1,1, 1, true, 'did some work for a site', 'facebook')
