package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;

public interface UserStorage {
    HashMap<Long, User> storage = new HashMap<>();

    User update(User user);

    void remove(Long id);

    User get(Long id);

    List<User> getAll();

    Boolean isExist(Long id);
}
