package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
public class RecommendationService {
    private final FilmDao filmDao;

    @Autowired
    public RecommendationService(FilmDao filmDao) {
        this.filmDao = filmDao;
    }

    public List<Film> getRecommendations(Long userId) {
        return filmDao.getRecommendations(userId);
    }
}
