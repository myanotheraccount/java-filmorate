package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements AbstractService<User> {
    private final InMemoryUserStorage userStorage;

    private Long id = 0L;

    @Autowired
    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private Long generateId() {
        return ++id;
    }

    @Override
    public User create(User user) {
        user.setId(0L);
        validate(user);
        user.setId(generateId());
        return userStorage.update(user);
    }

    @Override
    public User update(User user) {
        validate(user);
        if (userStorage.isExist(user.getId())) {
            return userStorage.update(user);
        }
        throw new NotFoundException(user + " id not found");
    }

    @Override
    public User get(Long id) {
        if (userStorage.isExist(id)) {
            return userStorage.get(id);
        }
        throw new NotFoundException(id + " id not found");
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Long userId, Long friendId) {
        if (userStorage.isExist(userId) && userStorage.isExist(friendId)) {
            User user = get(userId);
            user.addFriend(friendId);
            update(user);
            User friend = get(friendId);
            friend.addFriend(userId);
            update(friend);
        } else {
            throw new NotFoundException("not found");
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userStorage.isExist(userId) && userStorage.isExist(friendId)) {
            User user = get(userId);
            user.addFriend(friendId);
            update(user);
        }
    }

    public List<User> getFriends(Long userId) {
        if (userStorage.isExist(userId)) {
            User user = get(userId);
            return user.getFriends().stream().map(userStorage::get).collect(Collectors.toList());
        }
        throw new NotFoundException(userId + " id not found");
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        List<User> result = new ArrayList<>();
        Set<Long> otherFriends = userStorage.get(otherId).getFriends();
        for (Long friendId : userStorage.get(id).getFriends()) {
            if (otherFriends.contains(friendId)) {
                result.add(userStorage.get(friendId));
            }
        }
        return result;
    }

    public void validate(User user) {
        if ((user.getId() == null || user.getLogin().contains(" "))) {
            throw new ValidationException(user + " is invalid");
        }

        if (user.getName() == null || Objects.equals(user.getName(), "")) {
            user.setName(user.getLogin());
        }
    }
}
