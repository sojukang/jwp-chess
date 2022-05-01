drop table if exists square;
drop table if exists room;

create table room
(
    id bigint not null auto_increment,
    name varchar(255) not null,
    password varchar(255) not null,
    turn varchar(10) not null,
    unique (name),
    primary key (id)
);

create table square
(
    id bigint not null auto_increment,
    position varchar(5) not null,
    piece varchar(20) not null,
    room_id bigint not null,
    primary key (id),
    foreign key (room_id) references room (id)
);
