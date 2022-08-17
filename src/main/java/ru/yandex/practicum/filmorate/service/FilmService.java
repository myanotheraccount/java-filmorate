package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

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

    public List<Film> getPopular(Long count) {
        return filmDao.getPopular(count);
    }

    public void validate(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException(film + " is invalid");
        }
    }
}
