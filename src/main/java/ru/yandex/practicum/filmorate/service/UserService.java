package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements AbstractService<User> {
    @Autowired
    InMemoryUserStorage userStorage;

    @Override
    public User create(User user) {
        return userStorage.update(user);
    }

    @Override
    public void update(User user) throws NotFoundException {
        if (userStorage.isExist(user.getId())) {
            userStorage.update(user);
        } else {
            log.error(user + " id not found");
            throw new NotFoundException(user + " id not found");
        }
    }

    @Override
    public User get(Long id) throws NotFoundException {
        if (userStorage.isExist(id)) {
            return userStorage.get(id);
        }
        log.error(id + " id not found");
        throw new NotFoundException(id + " id not found");
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Long userId, Long friendId) throws NotFoundException {
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

    public void removeFriend(Long userId, Long friendId) throws NotFoundException {
        if (userStorage.isExist(userId) && userStorage.isExist(friendId)) {
            User user = get(userId);
            user.addFriend(friendId);
            update(user);
        }
    }

    public List<User> getFriends(Long userId) throws NotFoundException {
        if (userStorage.isExist(userId)) {
            User user = get(userId);
            return user.getFriends().stream().map(userStorage::get).collect(Collectors.toList());
        }
        log.error(userId + " id not found");
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
}
