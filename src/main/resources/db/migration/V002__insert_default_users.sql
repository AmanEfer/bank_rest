insert into users(first_name, last_name, phone_number, password)
values ('Oleg', 'Tinkoff', '8005553535', 'c3VwZXJAbGVnc3VwZXIhc2VjcmV0MjAyNQ=='); --password: super@leg

insert into roles(name)
values ('ROLE_USER'),
       ('ROLE_ADMIN');

insert into user_roles(user_id, role_id)
select u.id, r.id
from users u,
     roles r
where u.phone_number = '8005553535'
  and r.name = 'ROLE_ADMIN'
