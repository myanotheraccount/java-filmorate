package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
public class FriendsService {
    private final FriendsDao friendsDao;
    private final EventDao eventDao;

    @Autowired
    public FriendsService(FriendsDao friendsDao, EventDao eventDao) {
        this.friendsDao = friendsDao;
        this.eventDao = eventDao;
    }

    public void addFriend(Long userId, Long friendId) {
        try {
            friendsDao.addFriend(userId, friendId);
        } catch (Exception e) {
            throw new NotFoundException("Не удалось добавить друга");
        }
        eventDao.addEvent(new Event(userId, friendId, EventType.FRIEND, OperationType.ADD));
    }

    public void removeFriend(Long userId, Long friendId) {
        friendsDao.removeFriend(userId, friendId);
        eventDao.addEvent(new Event(userId, friendId, EventType.FRIEND, OperationType.REMOVE));
    }

    public List<User> getFriends(Long userId) {
        return friendsDao.getFriends(userId);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return friendsDao.getCommonFriends(id, otherId);
    }
}
