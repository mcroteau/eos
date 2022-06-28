create table if not exists users (
	id bigint PRIMARY KEY AUTO_INCREMENT,
	phone character varying(40) NOT NULL,
	email character varying(40) NOT NULL,
	password character varying(155) NOT NULL
);

create table if not exists roles (
	id bigint PRIMARY KEY AUTO_INCREMENT,
	name character varying(155) NOT NULL UNIQUE
);

create table if not exists user_roles(
	role_id bigint NOT NULL REFERENCES roles(id),
	user_id bigint NOT NULL REFERENCES users(id)
);

create table if not exists user_permissions(
	user_id bigint REFERENCES users(id),
	permission character varying(55)
);

insert into users(email, phone, password) values ('croteau.mike+uno@gmail.com','9079878652','a23fe49136f6d03528e2ae5bbced3a3129c8834dc51a97e41a25914aa8c9c320');
insert into users(email, phone, password) values ('croteau.mike+dos@gmail.com','9079878652','a23fe49136f6d03528e2ae5bbced3a3129c8834dc51a97e41a25914aa8c9c320');
insert into roles(name) values ('SUPER');
insert into roles(name) values ('BASIC');
insert into user_roles(user_id, role_id) values (1, 1);
insert into user_roles(user_id, role_id) values (2, 2);
insert into user_permissions(user_id, permission) values (1, 'users:maintenance:1');
insert into user_permissions(user_id, permission) values (1, 'users:maintenance:2');
insert into user_permissions(user_id, permission) values (2, 'users:maintenance:2');