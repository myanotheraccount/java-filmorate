package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.controller.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User update(User user);

    void remove(Long id);

    User get(Long id);

    List<User> getAll();

    Boolean isExist(Long id);
}
