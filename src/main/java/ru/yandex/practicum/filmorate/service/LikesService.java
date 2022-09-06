package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.dao.MarksDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Mark;
import ru.yandex.practicum.filmorate.model.OperationType;

@Service
@Slf4j
public class LikesService {
    private final EventDao eventDao;
    private final MarksDao marksDao;

    @Autowired
    public LikesService(MarksDao marksDao, EventDao eventDao) {
        this.eventDao = eventDao;
        this.marksDao = marksDao;
    }

    public void addLike(Long filmId, Long userId) {
        float existingMark = marksDao.getMark(filmId, userId);
        float newMark;
        if (existingMark < Mark.NEUTRAL_MARK) {
            newMark = Mark.NEUTRAL_MARK + Mark.LIKE_EQUIVALENT;
        } else {
            newMark = Math.min(existingMark + Mark.LIKE_EQUIVALENT, Mark.HIGHEST_MARK);
        }
        if (marksDao.addMark(filmId, userId, newMark)) {
            eventDao.addEvent(new Event(userId, filmId, EventType.LIKE, OperationType.ADD));
            log.info("Пользователь {} поставил лайк фильму {}.", userId, filmId);
        } else {
            throw new NotFoundException(
                    String.format("Не удалось добавить лайк, возможно фильм %d или пользователь %d не существуют.",
                            filmId, userId));
        }
    }

    public void removeLike(Long filmId, Long userId) {
        if (marksDao.removeMark(filmId, userId)) {
            eventDao.addEvent(new Event(userId, filmId, EventType.LIKE, OperationType.REMOVE));
            log.info("Пользователь {} удалил лайк фильму {}.", userId, filmId);
        } else {
            throw new NotFoundException(
                    String.format("Не удалось удалить лайк, возможно фильм %d или пользователь %d не существуют.",
                            filmId, userId));
        }
    }
}
