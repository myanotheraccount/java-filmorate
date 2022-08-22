package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.impl.RecommendationDao;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
public class RecommendationService {
    private final RecommendationDao recommendationDao;

    @Autowired
    public RecommendationService(RecommendationDao recommendationDao) {
        this.recommendationDao = recommendationDao;
    }

    public List<Film> getRecommendations(Long id) {
        return recommendationDao.getRecommendations(id);
    }
}

