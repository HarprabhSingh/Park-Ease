CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT uuidv7(),
    email_normalized VARCHAR(254) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email_normalized
        UNIQUE (email_normalized),
    CONSTRAINT ck_users_email_normalized
        CHECK (
            email_normalized = LOWER(BTRIM(email_normalized))
            AND email_normalized <> ''
        ),
    CONSTRAINT ck_users_display_name_not_blank
        CHECK (BTRIM(display_name) <> ''),
    CONSTRAINT ck_users_account_status
        CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'CLOSED'))
);

CREATE TABLE user_credentials (
    user_id UUID PRIMARY KEY,
    password_hash TEXT NOT NULL,
    password_changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_user_credentials_password_hash_not_blank
        CHECK (BTRIM(password_hash) <> '')
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_code VARCHAR(20) NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_roles
        PRIMARY KEY (user_id, role_code),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_user_roles_role_code
        CHECK (role_code IN ('DRIVER', 'OWNER', 'ADMIN'))
);
