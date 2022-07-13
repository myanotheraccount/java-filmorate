package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService implements AbstractService<Film> {
    private final InMemoryFilmStorage filmStorage;

    private final InMemoryUserStorage userStorage;

    private Long id = 0L;

    @Autowired
    public FilmService(InMemoryFilmStorage filmStorage, InMemoryUserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private Long generateId() {
        return ++id;
    }

    @Override
    public Film create(Film film) {
        film.setId(0L);
        validate(film);
        film.setId(generateId());
        return filmStorage.update(film);
    }

    @Override
    public Film update(Film film) {
        validate(film);
        if (filmStorage.isExist(film.getId())) {
            return filmStorage.update(film);
        }
        throw new NotFoundException(film + " id not found");
    }

    @Override
    public Film get(Long id) {
        if (filmStorage.isExist(id)) {
            return filmStorage.get(id);
        }
        throw new NotFoundException(id + " id not found");
    }

    @Override
    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(Long id, Long userId) {
        Film film = get(id);
        film.addLike(userId);
        update(film);
    }

    public void removeLike(Long id, Long userId) {
        Film film = get(id);
        if (userStorage.isExist(userId)) {
            film.removeLike(userId);
            update(film);
        } else {
            throw new NotFoundException(userId + " user id not found");
        }
    }

    public List<Film> getPopular(Long count) {
        List<Film> films = filmStorage.getAll();
        films.sort(Comparator.comparing(Film::getLikes).reversed());
        return films.stream().limit(count).collect(Collectors.toList());
    }

    public void validate(Film film) {
        if (film.getId() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException(film + " is invalid");
        }
    }
}
