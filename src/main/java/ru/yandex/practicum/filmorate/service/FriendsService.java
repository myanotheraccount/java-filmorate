package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
public class FriendsService {
    FriendsDao friendsDao;

    @Autowired
    public FriendsService(FriendsDao friendsDao) {
        this.friendsDao = friendsDao;
    }

    public void addFriend(Long userId, Long friendId) {
        try {
            friendsDao.addFriend(userId, friendId);
        } catch (Exception e) {
            throw new NotFoundException("Не удалось добавить друга");
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        friendsDao.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        return friendsDao.getFriends(userId);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return friendsDao.getCommonFriends(id, otherId);
    }
}
