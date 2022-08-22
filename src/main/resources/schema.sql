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
    FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS reviews
(
    review_id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    content VARCHAR(200),
    is_positive BOOLEAN,
    useful INTEGER,
    user_id INTEGER,
    film_id INTEGER,
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews_likes
(
    review_id   INTEGER,
    user_id INTEGER,
    is_positive INTEGER,
    FOREIGN KEY (review_id) REFERENCES reviews (review_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (review_id,user_id)
);

CREATE TABLE IF NOT EXISTS directors
(
    id   INTEGER PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS films_directors
(
    film_id     INTEGER,
    director_id INTEGER,
    FOREIGN KEY (film_id) REFERENCES films (id) ON DELETE CASCADE,
    FOREIGN KEY (director_id) REFERENCES directors (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);
