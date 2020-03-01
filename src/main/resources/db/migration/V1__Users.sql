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
insert into occupation_c(value,label) values('SelfEmployed','Self Employed');
insert into occupation_c(value,label) values('NoOccupation','No Occupation');

create table field_of_work_c(
  id serial primary key,
  value varchar not null unique,
  label varchar
);

insert into field_of_work_c(value,label) values('Business/Management','Business/Management');
insert into field_of_work_c(value,label) values('Customer Support/Call Center','Customer Support/Call Center');
insert into field_of_work_c(value,label) values('Law','Law');
insert into field_of_work_c(value,label) values('Education/Training','Education/Training');
insert into field_of_work_c(value,label) values('Finance/Banks','Finance/Banks');
insert into field_of_work_c(value,label) values('HR/Human Resources','HR/Human Resources');
insert into field_of_work_c(value,label) values('It/Engineering/Technical','It/Engineering/Technical');
insert into field_of_work_c(value,label) values('Logistics/Transportation','Logistics/Transportation');
insert into field_of_work_c(value,label) values('Marketing/Advertising/PR','Marketing/Advertising/PR');
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
  primary key(user_id,group_id)
);

create trigger set_timestamp_groups_users
before update on groups_users for each row execute procedure set_update_date();

create table invitations (
  id varchar not null primary key,
  user_id varchar not null references users(id),
  expires_at TIMESTAMP not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_invitations
before update on invitations for each row execute procedure set_update_date();

create table quizz_defs(
  id varchar not null primary key,
  name varchar not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_quizz_defs
before update on quizz_defs for each row execute procedure set_update_date();

create table quizzes (
  id varchar not null primary key,
  user_id varchar not null references users(id),
  quizz_def_id varchar not null references quizz_defs(id),
  description text not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_quizzes
before update on quizzes for each row execute procedure set_update_date();

create table question_sets (
  id varchar not null primary key,
  title text not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_question_sets
before update on question_sets for each row execute procedure set_update_date();

create table quizz_defs_question_sets(
  quizz_def_id varchar not null references quizz_defs(id),
  question_set_id varchar not null references question_sets(id),
  questions_count int,
  created timestamptz not null default now(),
  updated timestamptz not null default now(),
  primary key(quizz_def_id,question_set_id)
);

create trigger set_timestamp_quizz_defs_question_sets
before update on quizz_defs_question_sets for each row execute procedure set_update_date();

create table questions (
  id varchar not null primary key,
  question_set_id varchar references question_sets(id),
  content text not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_questions
before update on questions for each row execute procedure set_update_date();

create table quizzes_questions(
  quizz_id varchar not null references quizzes(id),
  question_id varchar not null references questions(id),
  created timestamptz not null default now(),
  updated timestamptz not null default now(),
  primary key(quizz_id,question_id)
);

create trigger set_timestamp_quizzes_questions
before update on quizzes_questions for each row execute procedure set_update_date();

create table quizz_answers(
  id serial primary key,
  question_id varchar not null references questions(id),
  quizz_id varchar not null references quizzes(id),
  answer text not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_quizz_answers
before update on quizz_answers for each row execute procedure set_update_date();

create table answers (
  id varchar not null primary key,
  question_id varchar not null references questions(id),
  content text not null,
  is_correct BOOLEAN,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_answers
before update on answers for each row execute procedure set_update_date();

create table course_types(
  id serial primary key,
  name varchar not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_course_types
before update on course_types for each row execute procedure set_update_date();

insert into course_types(id,name) values(1,'Programming');
insert into course_types(id,name) values(2,'Testing');
insert into course_types(id,name) values(3,'Networking');
insert into course_types(id,name) values(4,'Ops');
insert into course_types(id,name) values(5,'Others');

create table courses (
  id serial primary key,
  name varchar not null,
  course_type_id smallint not null references course_types(id),
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_courses
before update on courses for each row execute procedure set_update_date();

insert into courses(id,name,course_type_id) values(1,'Java',1);
insert into courses(id,name,course_type_id) values(3,'.Net Programming',1);
insert into courses(id,name,course_type_id) values(4,'Python',1);
insert into courses(id,name,course_type_id) values(5,'Manual Testing & Intro in Automation',2);
insert into courses(id,name,course_type_id) values(6,'Frontend',1);
insert into courses(id,name,course_type_id) values(7,'DevOps',4);
insert into courses(id,name,course_type_id) values(8,'UI/UX Design',5);
insert into courses(id,name,course_type_id) values(9,'Network & Infrastructure',4);
insert into courses(id,name,course_type_id) values(10,'Intro in Programming',1);
insert into courses(id,name,course_type_id) values(11,'Intro in Databases',1);
insert into courses(id,name,course_type_id) values(12,'Intro in Networking',4);
insert into courses(id,name,course_type_id) values(14,'Testing Automation',2);

create table users_courses (
  user_id varchar not null references users(id),
  course_id smallint not null references courses(id),
  created timestamptz not null default now(),
  updated timestamptz not null default now(),
  primary key(user_id,course_id)
);

create trigger set_timestamp_users_courses
before update on users_courses for each row execute procedure set_update_date();

create table quizz_defs_courses(
  quizz_def_id varchar not null references quizz_defs(id),
  course_id smallint references courses(id),
  created timestamptz not null default now(),
  updated timestamptz not null default now(),
  primary key(quizz_def_id,course_id)
);

create trigger set_timestamp_users_quizz_defs_courses
before update on quizz_defs_courses for each row execute procedure set_update_date();

create table locations (
  id serial primary key,
  name varchar,
  created timestamptz not null default now(),
  updated timestamptz not null default now()
);

create trigger set_timestamp_locations
before update on locations for each row execute procedure set_update_date();

insert into locations(id,name) values(1,'Iasi');
insert into locations(id,name) values(2,'Bucuresti');

create table locations_courses(
  location_id smallint not null,
  course_id smallint not null,
  created timestamptz not null default now(),
  updated timestamptz not null default now(),
  primary key (location_id,course_id)
);

create trigger set_timestamp_locations_courses
before update on locations_courses for each row execute procedure set_update_date();

insert into locations_courses(location_id,course_id) values(1,1);
insert into locations_courses(location_id,course_id) values(1,2);
insert into locations_courses(location_id,course_id) values(2,1);
insert into locations_courses(location_id,course_id) values(3,1);
insert into locations_courses(location_id,course_id) values(4,1);
insert into locations_courses(location_id,course_id) values(4,2);
insert into locations_courses(location_id,course_id) values(5,1);
insert into locations_courses(location_id,course_id) values(5,2);
insert into locations_courses(location_id,course_id) values(6,1);
insert into locations_courses(location_id,course_id) values(6,2);
insert into locations_courses(location_id,course_id) values(7,1);
insert into locations_courses(location_id,course_id) values(8,1);
insert into locations_courses(location_id,course_id) values(9,1);
insert into locations_courses(location_id,course_id) values(10,1);
insert into locations_courses(location_id,course_id) values(10,2);
insert into locations_courses(location_id,course_id) values(11,1);
insert into locations_courses(location_id,course_id) values(12,1);
insert into locations_courses(location_id,course_id) values(14,1);
insert into locations_courses(location_id,course_id) values(14,2);

insert into users(id,email,first_name,last_name,birthday,city,phone,occupation,
  field_of_work,english_level,it_experience,experience_description,heard_from) values (
  'user0','user0@example.com','John','User','1980-01-30 00:00:00Z',
  'Iasi','0740012345',1,1,1,true,'did some work for a site','facebook');

insert into groups_users(group_id,user_id) values('student','user0');

