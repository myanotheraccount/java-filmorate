package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import ru.yandex.practicum.filmorate.dao.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.LikesService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final FilmDao filmDao;
    private final GenreDao genreDao;
    private final LikesService likesService;
    private final MpaDao mpaDao;
    private final UserDao userDao;
    private final FriendsDao friendsDao;

    private User createUser() {
        User user = new User();
        user.setName("user");
        user.setEmail("email@email.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2022, 1, 1));
        return userDao.createUser(user);
    }

    private Film createFilm() {
        Film film = new Film();
        film.setName("film");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2022, 1, 1));
        film.setDuration(100);
        film.setMpa(mpaDao.getMpa(1L));
        film.setRate(10L);
        film.setGenres(List.of(genreDao.getGenre(1L)));
        return filmDao.createFilm(film);
    }

    @BeforeEach
    @AfterEach
    public void execute() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate,
                "films_genres",
                "friendship",
                "likes",
                "users",
                "films"
        );
    }

    @Test
    public void testFindUser() {
        createUser();
        long id = createUser().getId();
        User user = userDao.findUserById(id);
        assertEquals(user.getLogin(), "login");
        assertEquals(userDao.getAll().size(), 2);
    }

    @Test
    public void updateUsers() {
        long id = createUser().getId();
        User user = userDao.findUserById(id);
        user.setName("test");
        userDao.updateUser(user);
        assertEquals(userDao.findUserById(id).getName(), "test");
    }

    @Test
    public void checkFriends() {
        long userId = createUser().getId();
        long user2Id = createUser().getId();
        long user3Id = createUser().getId();
        friendsDao.addFriend(user2Id, userId);
        friendsDao.addFriend(user3Id, userId);
        assertEquals(friendsDao.getFriends(user3Id).size(), 1);
        assertEquals(friendsDao.getCommonFriends(user2Id, user3Id).size(), 1);
        friendsDao.removeFriend(user2Id, userId);
        assertEquals(friendsDao.getFriends(user2Id).size(), 0);
    }

    @Test
    public void getAllMpasAndGenres() {
        assertEquals(mpaDao.getAllMpa().size(), 5);
        assertEquals(genreDao.getAllGenres().size(), 6);
    }

    @Test
    public void findFilms() {
        createFilm();
        long id = createFilm().getId();
        Film film = filmDao.getFilmById(id);
        assertEquals(film.getName(), "film");
        assertEquals(filmDao.getAll().size(), 2);
    }

    @Test
    public void getPopular() {
        Film film = createFilm();
        Film film2 = createFilm();

        User user1 = createUser();
        User user2 = createUser();

        likesService.addLike(film.getId(), user1.getId());
        likesService.addLike(film.getId(), user2.getId());

        assertEquals(filmDao.getPopular(1L).get(0).getId(), film.getId());

        likesService.removeLike(film.getId(), user1.getId());
        likesService.removeLike(film.getId(), user2.getId());
        likesService.addLike(film2.getId(), user1.getId());

        assertEquals(filmDao.getPopular(1L).get(0).getId(), film2.getId());
    }
}
