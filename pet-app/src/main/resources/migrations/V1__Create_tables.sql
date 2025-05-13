CREATE TABLE IF NOT EXISTS owners
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    birthdate  DATE,
    password   VARCHAR(60) NOT NULL,
    role       VARCHAR(20) NOT NULL
    );

CREATE TABLE IF NOT EXISTS pets
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    birthdate  DATE,
    breed      VARCHAR(50),
    color      VARCHAR(50),
    owner_id   BIGINT,
    FOREIGN KEY (owner_id) REFERENCES owners (id) ON DELETE SET NULL
    );

CREATE TABLE IF NOT EXISTS pet_friends
(
    pet_id    int NOT NULL,
    friend_id int NOT NULL,
    PRIMARY KEY (pet_id, friend_id),
    FOREIGN KEY (pet_id) REFERENCES pets (id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES pets (id) ON DELETE CASCADE
    );
