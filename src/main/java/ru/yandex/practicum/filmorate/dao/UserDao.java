package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserDao {
    User findUserById(Long id);

    User createUser(User user);

    User updateUser(User user);

    List<User> getAll();

    void delete(Long id);
}
