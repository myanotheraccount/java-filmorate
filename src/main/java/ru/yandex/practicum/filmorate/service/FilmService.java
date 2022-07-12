package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService implements AbstractService<Film> {
    @Autowired
    InMemoryFilmStorage filmStorage;

    @Autowired
    InMemoryUserStorage userStorage;

    @Override
    public Film create(Film film) {
        return filmStorage.update(film);
    }

    @Override
    public void update(Film film) throws NotFoundException {
        if (filmStorage.isExist(film.getId())) {
            filmStorage.update(film);
        } else {
            log.error(film + " id not found");
            throw new NotFoundException(film + " id not found");
        }
    }

    @Override
    public Film get(Long id) throws NotFoundException {
        if (filmStorage.isExist(id)) {
            return filmStorage.get(id);
        }
        log.error(id + " id not found");
        throw new NotFoundException(id + " id not found");
    }

    @Override
    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(Long id, Long userId) throws NotFoundException {
        Film film = get(id);
        film.addLike(userId);
        update(film);
    }

    public void removeLike(Long id, Long userId) throws NotFoundException {
        Film film = get(id);
        if (userStorage.isExist(userId)) {
            film.removeLike(userId);
            update(film);
        } else {
            log.error(userId + " user id not found");
            throw new NotFoundException(userId + " user id not found");
        }
    }

    public List<Film> getPopular(Long count) {
        List<Film> films = filmStorage.getAll();
        films.sort(Comparator.comparing(Film::getLikes).reversed());
        return films.stream().limit(count).collect(Collectors.toList());
    }
}
