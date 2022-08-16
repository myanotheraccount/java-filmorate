CREATE TABLE IF NOT EXISTS mpas
(
    mpa_id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    mpa_name VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS films
(
    id           INTEGER PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(100),
    description  TEXT,
    release_date DATE,
    duration     INTEGER,
    rate         INTEGER,
    mpa_id       INTEGER,
    FOREIGN KEY (mpa_id) REFERENCES mpas (mpa_id)
);

CREATE TABLE IF NOT EXISTS genres
(
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS films_genres
(
    film_id  INTEGER,
    genre_id INTEGER,
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (id),
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS users
(
    id        INTEGER PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(100),
    email     VARCHAR(100),
    login     VARCHAR(100),
    birthdate DATE
);

CREATE TABLE IF NOT EXISTS friendship
(
    user_id   INTEGER,
    friend_id INTEGER,
    status    BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    film_id INTEGER,
    user_id INTEGER,
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);
