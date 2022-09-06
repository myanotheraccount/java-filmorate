package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmDao {
    Film createFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getAll();

    Film updateFilm(Film film);

    List<Film> getPopular(Long count);

    List<Film> getByFilter(Long directorId, String sortBy);

    List<Film> getFilmsByParams(String query, List<String> queryParams);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getPopular(long count, Optional<Integer> genreId, Optional<Integer> year);

    List<Film> getRecommendations(Long userId);

    void delete(Long id);
}
