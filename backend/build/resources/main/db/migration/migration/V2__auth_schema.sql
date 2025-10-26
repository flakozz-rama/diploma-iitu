-- Требуемые расширения
CREATE EXTENSION IF NOT EXISTS "pgcrypto";  -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "citext";    -- тип CITEXT

-- ============================================
-- Auth schema: users, profiles, roles, user_roles,
-- refresh_tokens, oauth_accounts
-- ============================================

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
                                     id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email             CITEXT NOT NULL UNIQUE,
    password_hash     VARCHAR(100) NOT NULL,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    status            VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / BLOCKED / DELETED
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

-- Профиль пользователя (1:1)
CREATE TABLE IF NOT EXISTS profiles (
                                        user_id     UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    first_name  VARCHAR(100),
    last_name   VARCHAR(100),
    avatar_url  TEXT,
    gender      VARCHAR(20),
    birth_date  DATE,
    height_cm   INTEGER,
    weight_kg   NUMERIC(6,2),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- Роли
CREATE TABLE IF NOT EXISTS roles (
                                     id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code       VARCHAR(30) NOT NULL UNIQUE,  -- USER, COACH, ADMIN
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- Пользователь <-> Роль (M:N)
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id    UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
    );

-- Refresh токены
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(128) NOT NULL,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked         BOOLEAN NOT NULL DEFAULT FALSE,
    rotated_from_id UUID NULL REFERENCES refresh_tokens(id) ON DELETE SET NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user   ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires ON refresh_tokens (expires_at);

-- OAuth аккаунты
CREATE TABLE IF NOT EXISTS oauth_accounts (
                                              id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider         VARCHAR(20) NOT NULL, -- 'GOOGLE' | 'APPLE'
    provider_user_id VARCHAR(128) NOT NULL,
    email            CITEXT,
    name             VARCHAR(200),
    avatar_url       TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_login_at    TIMESTAMPTZ
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_oauth_provider_user
    ON oauth_accounts (provider, provider_user_id);

-- Сид ролей
INSERT INTO roles (id, code, name)
VALUES
    (gen_random_uuid(), 'USER',  'User'),
    (gen_random_uuid(), 'COACH', 'Coach'),
    (gen_random_uuid(), 'ADMIN', 'Administrator')
    ON CONFLICT (code) DO NOTHING;

-- Триггер updated_at
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_updated ON users;
CREATE TRIGGER trg_users_updated
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS trg_profiles_updated ON profiles;
CREATE TRIGGER trg_profiles_updated
    BEFORE UPDATE ON profiles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
