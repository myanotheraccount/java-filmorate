package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.dao.impl.RecommendationDao;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private final RecommendationDao recommendationDao;
    private final FilmDao filmDao;

    @Autowired
    public RecommendationService(RecommendationDao recommendationDao, FilmDao filmDao) {
        this.recommendationDao = recommendationDao;
        this.filmDao = filmDao;
    }

    public List<Film> getRecommendations(Long id) {
        return recommendationDao.getRecommendations(id).stream().map(filmDao::getFilmById).collect(Collectors.toList());
    }
}

