CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    name  VARCHAR(255)                                    NOT NULL,
    email VARCHAR(256)                                    NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS requests
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    description VARCHAR(300),
    created     TIMESTAMP WITHOUT TIME ZONE,
    owner_id    BIGINT REFERENCES users (id)
    );

CREATE TABLE IF NOT EXISTS items
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    name         VARCHAR(255)                                    NOT NULL,
    description  VARCHAR(300)                                    NOT NULL,
    is_available BOOLEAN                                         NOT NULL,
    owner_id     BIGINT REFERENCES users (id) ON DELETE CASCADE,
    request_id   BIGINT REFERENCES requests (id)
    );

CREATE TABLE IF NOT EXISTS bookings
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date   TIMESTAMP WITHOUT TIME ZONE,
    status     VARCHAR,
    item_id    BIGINT REFERENCES items (id) ON DELETE CASCADE,
    booker_id  BIGINT REFERENCES users (id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY NOT NULL,
    text      VARCHAR(300),
    created   TIMESTAMP WITHOUT TIME ZONE,
    item_id   BIGINT REFERENCES items (id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users (id) ON DELETE CASCADE
    );