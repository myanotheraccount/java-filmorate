package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorDao {
    Director addDirector(Director director);

    Director getDirectorById(Long id);

    List<Director> getAll();

    Director updateDirector(Director director);

    void delete(Long id);
}
