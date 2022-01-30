-- your create schema sql --
create table todos (
	id bigint PRIMARY KEY AUTO_INCREMENT,
	title character varying(254) NOT NULL
);

insert into todos values (1, 'Exercise');
insert into todos values (2, 'Finish reading');
insert into todos values (3, 'Hopefully enjoy');