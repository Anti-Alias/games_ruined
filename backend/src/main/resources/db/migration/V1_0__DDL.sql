CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    salt TEXT NOT NULL
);

CREATE TYPE RESOURCE_TYPE AS ENUM('TEXTURE', 'SOUND');

CREATE TABLE "resource" (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    type RESOURCE_TYPE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE "platform" (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    simple_name TEXT NOT NULL
);

CREATE TABLE "game" (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    platform_id INTEGER NOT NULL,
    FOREIGN KEY (platform_id) REFERENCES "platform"(id)
);

-- Indexes
CREATE INDEX ON "user"(id);
CREATE INDEX ON "user"(email);
CREATE INDEX ON "resource"(id);
CREATE INDEX ON "resource"(user_id);
CREATE INDEX ON "resource"(type);
CREATE INDEX ON "platform"(id);
CREATE INDEX ON "platform"(name);
CREATE INDEX ON "platform"(simple_name);
CREATE INDEX ON "game"(id);
CREATE INDEX ON "game"(name);
CREATE INDEX ON "game"(platform_id);
