create table if not exists users
(
    id           bigserial primary key,
    first_name   varchar(128) not null,
    last_name    varchar(128) not null,
    phone_number varchar(10)  not null unique,
    password     varchar(255) not null,
    created_at   timestamp default now(),
    updated_at   timestamp default now()
);

create table if not exists roles
(
    id   bigserial primary key,
    name varchar(32) not null unique
);

create table if not exists user_roles
(
    user_id bigserial not null references users (id) on delete cascade,
    role_id bigserial not null references roles (id) on delete cascade,
    primary key (user_id, role_id)
);

create table if not exists cards
(
    id                    bigserial primary key,
    encrypted_card_number varchar(255)   not null,
    last4                 varchar(4)     not null,
    encrypted_placeholder varchar(255)   not null,
    expiration_date       date           not null,
    status                varchar(32)    not null default 'ACTIVE' check (status in ('ACTIVE', 'BLOCKED', 'EXPIRED', 'REQUESTED_BLOCKED')),
    balance               numeric(14, 2) not null default 0.00,
    created_at            timestamp               default now(),
    updated_at            timestamp               default now(),
    user_id               bigserial      not null references users (id) on delete cascade
);

