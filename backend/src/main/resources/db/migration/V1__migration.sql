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

-- Inserts some initial data
INSERT INTO "platform" (id, name, simple_name) VALUES
    (0, 'Windows', 'PC'),
    (1, 'Mac', 'OSX'),
    (2, 'Nintendo Entertainment System', 'NES'),
    (3, 'Sega Genesis', 'Genesis'),
    (4, 'Playstation 1', 'PS1'),
    (5, 'Nintendo 64', 'N64');