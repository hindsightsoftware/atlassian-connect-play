# --- !Ups

create table ac_host (
  id                        bigint not null,
  key                       varchar(255) not null,
  public_key                varchar(512) not null,
  base_url                  varchar(512) not null,
  name                      varchar(255),
  description               varchar(255),
  constraint uq_host_application_key unique (key),
  constraint uq_host_application_base_url unique (base_url),
  constraint pk_host_application primary key (id))
;

create sequence ac_host_seq;

# --- !Downs

drop table if exists ac_host cascade;

drop sequence if exists ac_host_seq;
