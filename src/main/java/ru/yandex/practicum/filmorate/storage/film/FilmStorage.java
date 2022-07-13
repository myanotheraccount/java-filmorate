package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;

public interface FilmStorage {
    HashMap<Long, Film> storage = new HashMap<>();

    Film update(Film film);

    void remove(Long id);

    Film get(Long id);

    List<Film> getAll();

    Boolean isExist(Long id);
}
