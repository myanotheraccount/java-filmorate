package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmoRateApplicationTests {
    private final UserDao userDao;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("user");
        user.setEmail("user@user.ru");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2022, 1, 1));
        userDao.createUser(user);
    }

    @Test
    public void testFindUserById() {
        User user = userDao.findUserById(1L);
        assertEquals(user.getLogin(), "login");
    }

    @Test
    public void updateUser() {
        User user = userDao.findUserById(1L);
        user.setName("test");
        userDao.updateUser(user);
        assertEquals(userDao.findUserById(1L).getName(), "test");
    }

    @Test
    public void toggleFriends() {
        User user = new User();
        user.setName("user2");
        user.setEmail("user2@user.ru");
        user.setLogin("login2");
        user.setBirthday(LocalDate.of(2020, 1, 1));
        userDao.createUser(user);
        userDao.addFriend(1L,2L);
        assertEquals(userDao.getFriends(1L).size(), 1);
        userDao.removeFriend(1L,2L);
        assertEquals(userDao.getFriends(1L).size(), 0);
    }
}
