package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmDao {
    Film createFilm(Film film);

    Film getFilmById(Long id);

    List<Film> getAll();

    Film updateFilm(Film film);

    List<Film> getPopular(Long count);

    void delete(Long id);
}
