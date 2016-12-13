# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table basic_auth_user (
  username                  varchar(255) not null,
  password                  varchar(255),
  constraint pk_basic_auth_user primary key (username))
;

create table user (
  username                  varchar(255) not null,
  password                  varchar(255),
  phpssid                   varchar(255),
  constraint pk_user primary key (username))
;

create sequence basic_auth_user_seq;

create sequence user_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists basic_auth_user;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists basic_auth_user_seq;

drop sequence if exists user_seq;
