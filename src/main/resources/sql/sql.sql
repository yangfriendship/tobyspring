create table users(
id varchar(15) primary key,
name varchar(10),
password varchar(15)
);

create table users(
id varchar(15) primary key,
name varchar(10) not null,
password varchar(15) not null,
level int not null,
login int not null,
recommend int not null
);