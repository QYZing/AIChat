create table users
(
    id           int auto_increment
        primary key,
    username     varchar(50)                         not null,
    userPassword varchar(255)                        not null,
    created_at   timestamp default CURRENT_TIMESTAMP null,
    constraint username
        unique (username),
    constraint users_pk
        unique (username)
);

create table sessions
(
    id             varchar(36)                         not null
        primary key,
    user_id        int                                 not null,
    assistant_type varchar(20)                         not null,
    created_at     timestamp default CURRENT_TIMESTAMP null,
    constraint sessions_ibfk_1
        foreign key (user_id) references users (id)
);

create table messages
(
    id         int auto_increment
        primary key,
    session_id varchar(36)                          not null,
    content    text                                 not null,
    role       enum ('user', 'assistant')           not null,
    created_at timestamp  default CURRENT_TIMESTAMP null,
    is_current tinyint(1) default 1                 null,
    is_temp    int        default 0                 null,
    constraint messages_pk
        unique (session_id, role),
    constraint messages_ibfk_1
        foreign key (session_id) references sessions (id)
);

create index user_id
    on sessions (user_id);

