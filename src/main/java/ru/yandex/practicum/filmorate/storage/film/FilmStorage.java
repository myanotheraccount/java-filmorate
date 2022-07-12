package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.controller.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film update(Film film) ;

    void remove(Long id);

    Film get(Long id);

    List<Film> getAll();

    Boolean isExist(Long id);
}
