MERGE INTO USERS (ID, LOGIN, NAME, EMAIL, BIRTHDATE)
    VALUES (1, 'user1', 'user1', 'user1@test.com', '2000-01-01'),
           (2, 'user2', 'user2', 'user2@test.com', '2000-01-02'),
           (3, 'user3', 'user3', 'user3@test.com', '2000-01-03'),
           (4, 'user4', 'user4', 'user4@test.com', '2000-01-04'),
           (5, 'user5', 'user5', 'user5@test.com', '2000-01-05');

MERGE INTO FILMS (ID, NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)
    VALUES (1, 'film 1', 'film 1', '2000-01-01', 120, 1),
           (2, 'film 2', 'film 2', '2000-01-02', 120, 1),
           (3, 'film 3', 'film 3', '2000-01-03', 120, 1),
           (4, 'film 4', 'film 4', '2000-01-04', 120, 1),
           (5, 'film 5', 'film 5', '2000-01-05', 120, 1),
           (6, 'film 6', 'film 6', '2000-01-06', 120, 1),
           (7, 'film 7', 'film 7', '2000-01-07', 120, 1);

MERGE INTO LIKES (FILM_ID, USER_ID, MARK_VALUE)
    VALUES (1, 1, 7.0),
           (1, 2, 7.0),
           (1, 5, 5.0),
           (2, 2, 9.0),
           (2, 3, 7.0),
           (3, 1, 5.0),
           (3, 3, 4.0),
           (3, 5, 3.0),
           (4, 1, 8.0),
           (4, 5, 5.7),
           (5, 4, 4.0),
           (5, 5, 9.0);