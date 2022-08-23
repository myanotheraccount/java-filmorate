package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dao.LikesDao;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

@Service
public class LikesService {
    private final LikesDao likesDao;
    private final EventDao eventDao;

    @Autowired
    public LikesService(LikesDao likesDao, EventDao eventDao) {
        this.likesDao = likesDao;
        this.eventDao = eventDao;
    }

    public void addLike(Long filmId, Long userId) {
        likesDao.addLike(filmId, userId);
        eventDao.addEvent(new Event(userId, filmId, EventType.LIKE, OperationType.ADD));
    }

    public void removeLike(Long filmId, Long userId) {
        likesDao.removeLike(filmId, userId);
        eventDao.addEvent(new Event(userId, filmId, EventType.LIKE, OperationType.REMOVE));
    }
}
