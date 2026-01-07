CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO profile(id,name,username, password, status,visible,created_date)
VALUES ('ba765b2b-53eb-410f-a92c-ca451681bae1','Admin',
        'muzaffarmike2@gmail.com','$2a$10$PTvhrTpWQr9W53wIVhlLGeww8hDMKm8s9.LrS.uwjoONsysbkW6lO',
        'ACTIVE',true, now());

INSERT INTO profile_role(id, profile_id, roles, created_date)
VALUES ('459af5b7-90c0-4553-b67d-a3e9249d0399','ba765b2b-53eb-410f-a92c-ca451681bae1','ROLE_ADMIN',now()),
       ('bd18de10-e543-427d-9277-dffe72ba7872','ba765b2b-53eb-410f-a92c-ca451681bae1','ROLE_USER', now());