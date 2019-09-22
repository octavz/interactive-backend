create function set_update_date() returns trigger as $set_update_date$
begin
  new.updated := current_timestamp;
  return new;
end;
$set_update_date$ language plpgsql;

create table english_level_c(
    id serial primary key,
    value varchar(200) not null unique,
    label varchar(200)
);

insert into english_level_c(value,label) values('Beginner','Beginner');
insert into english_level_c(value,label) values('Average','Average');
insert into english_level_c(value,label) values('Advanced','Advanced');

create table occupation_c(
    id serial primary key,
    value varchar(200) not null unique,
    label varchar(200)
);

insert into occupation_c(value,label) values('Employee','Employee');
insert into occupation_c(value,label) values('Student','Student');
insert into occupation_c(value,label) values('SelfEmployed','SelfEmployed');
insert into occupation_c(value,label) values('NoOccupation','NoOccupation');

create table field_of_work_c(
    id serial primary key,
    value varchar(200) not null unique,
    label varchar(200)
);

insert into field_of_work_c(value,label) values('Business/Management','Business/Management');
insert into field_of_work_c(value,label) values('Customer Support/Call Center','Customer
Support/Call Center');
insert into field_of_work_c(value,label) values('Law','Law');
insert into field_of_work_c(value,label) values('Education/Training','Education/Training');
insert into field_of_work_c(value,label) values('Finance/Banks','Finance/Banks');
insert into field_of_work_c(value,label) values('HR/Human Resources','HR/Human Resources');
insert into field_of_work_c(value,label) values('It/Engineering/Technical',
'It/Engineering/Technical');
insert into field_of_work_c(value,label) values('Logistics/Transportation',
'Logistics/Transportation');
insert into field_of_work_c(value,label) values('Marketing/Advertising/PR',
'Marketing/Advertising/PR');
insert into field_of_work_c(value,label) values('Medical/Health','Medical/Health');
insert into field_of_work_c(value,label) values('Production','Production');
insert into field_of_work_c(value,label) values('Services/Sales','Services/Sales');
insert into field_of_work_c(value,label) values('Another','Another');

create table users (
    id uuid primary key,
    email varchar(200) unique not null,
    first_name varchar(200) not null,
    last_name varchar(200) not null,
    birthday timestamp not null,
    city varchar(200) not null,
    phone varchar(200) not null,
    occupation smallint not null references occupation_c(id),
    field_of_work smallint not null references field_of_work_c(id),
    english_level smallint not null references english_level_c(id),
    it_experience boolean not null,
    experience_description text,
    heard_from varchar(200) not null references heard_from_c(id) ,
    created timestamptz not null default now(),
    updated timestamptz not null default now()
);

create trigger set_timestamp_users
before update on users
for each row execute procedure set_update_date();
