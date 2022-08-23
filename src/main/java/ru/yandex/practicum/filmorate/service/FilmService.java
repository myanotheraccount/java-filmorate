package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class FilmService implements AbstractService<Film> {

    private final FilmDao filmDao;

    @Autowired
    public FilmService(FilmDao filmDao) {
        this.filmDao = filmDao;
    }

    @Override
    public Film create(Film film) {
        validate(film);
        return filmDao.createFilm(film);
    }

    @Override
    public Film update(Film film) {
        validate(film);
        return filmDao.updateFilm(film);
    }

    @Override
    public Film get(Long id) {
        return filmDao.getFilmById(id);
    }

    @Override
    public List<Film> getAll() {
        return filmDao.getAll();
    }

    @Override
    public void delete(Long id) {
        filmDao.delete(id);
    }

    public List<Film> getPopular(long count, Optional<Integer> genreId, Optional<Integer> year) {
        return filmDao.getPopular(count, genreId, year);
    }

    public List<Film> getByFilter(Long directorId, String sortBy) {
        return filmDao.getByFilter(directorId, sortBy);
    }

    public List<Film> getFilmsByParams(String query, List<String> queryParams) {
        return filmDao.getFilmsByParams(query, queryParams);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmDao.getCommonFilms(userId, friendId);
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException(film + " is invalid");
        }
    }
}