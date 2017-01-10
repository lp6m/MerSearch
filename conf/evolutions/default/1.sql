# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table basic_auth_user (
  username                  varchar(255) not null,
  password                  varchar(255),
  constraint pk_basic_auth_user primary key (username))
;

create table manage_item (
  itemid                    varchar(255) not null,
  username                  varchar(255),
  itemjson                  TEXT,
  ignoreflag                boolean,
  zaiko                     integer,
  constraint pk_manage_item primary key (itemid))
;

create table user (
  username                  varchar(255) not null,
  password                  varchar(255),
  global_access_token       TEXT,
  access_token              TEXT,
  sellerid                  varchar(255),
  slackurl                  varchar(255),
  channel                   varchar(255),
  constraint pk_user primary key (username))
;

create sequence basic_auth_user_seq;

create sequence manage_item_seq;

create sequence user_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists basic_auth_user;

drop table if exists manage_item;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists basic_auth_user_seq;

drop sequence if exists manage_item_seq;

drop sequence if exists user_seq;

