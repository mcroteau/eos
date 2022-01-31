-- your create schema sql --
create table todos (
	id bigint PRIMARY KEY AUTO_INCREMENT,
	title character varying(254) NOT NULL
);