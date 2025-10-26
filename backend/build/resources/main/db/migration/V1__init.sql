-- Базовые расширения, нужны до всех таблиц
CREATE EXTENSION IF NOT EXISTS pgcrypto; -- для gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS citext;   -- для email CITEXT
