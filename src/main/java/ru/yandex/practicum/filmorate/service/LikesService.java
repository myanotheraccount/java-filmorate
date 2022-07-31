package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.LikesDao;

@Service
public class LikesService {
    private final LikesDao likesDao;

    @Autowired
    public LikesService(LikesDao likesDao) {
        this.likesDao = likesDao;
    }

    public void addLike(Long filmId, Long userId) {
        likesDao.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        likesDao.removeLike(filmId, userId);
    }
}
