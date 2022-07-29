package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public interface FilmDao {
    Film createFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getAll();

    Film updateFilm(Film film);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getPopular(Long count);

    Mpa getMpa(Long mpaId);

    List<Mpa> getAllMpa();

    Genre getGenre(Long genre);

    List<Genre> getAllGenres();
}
