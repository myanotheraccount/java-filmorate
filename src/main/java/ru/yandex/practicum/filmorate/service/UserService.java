package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Objects;

@Service
public class UserService implements AbstractService<User> {
    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User create(User user) {
        validate(user);
        return userDao.createUser(user);
    }

    @Override
    public User update(User user) {
        validate(user);
        return userDao.updateUser(user);
    }

    @Override
    public User get(Long id) {
        return userDao.findUserById(id);
    }

    @Override
    public List<User> getAll() {
        return userDao.getAll();
    }

    @Override
    public void delete(Long id) {
        userDao.delete(id);
    }

    public void validate(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException(user + " is invalid");
        }

        if (user.getName() == null || Objects.equals(user.getName(), "")) {
            user.setName(user.getLogin());
        }
    }
}
