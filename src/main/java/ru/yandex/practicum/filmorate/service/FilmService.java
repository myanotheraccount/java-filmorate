package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public void addLike(Long filmId, Long userId) {
        filmDao.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmDao.removeLike(filmId, userId);
    }

    public List<Film> getPopular(Long count) {
        return filmDao.getPopular(count);
    }

    public Mpa getMpa(Long mpaId) {
        return filmDao.getMpa(mpaId);
    }

    public List<Mpa> getAllMpa() {
        return filmDao.getAllMpa();
    }

    public Genre getGenre(Long genreId) {
        return filmDao.getGenre(genreId);
    }

    public List<Genre> getAllGenres() {
        return filmDao.getAllGenres();
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException(film + " is invalid");
        }
    }
}
