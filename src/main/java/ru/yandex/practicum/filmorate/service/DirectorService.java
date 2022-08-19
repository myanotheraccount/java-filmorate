package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Service
public class DirectorService implements AbstractService<Director> {
    private final DirectorDao directorDao;

    public DirectorService(DirectorDao directorDao) {
        this.directorDao = directorDao;
    }

    @Override
    public Director create(Director director) {
        validate(director);
        return directorDao.addDirector(director);
    }

    @Override
    public Director update(Director director) {
        return directorDao.updateDirector(director);
    }

    @Override
    public Director get(Long id) {
        return directorDao.getDirectorById(id);
    }

    @Override
    public List<Director> getAll() {
        return directorDao.getAll();
    }

    @Override
    public void delete(Long id) {
        directorDao.delete(id);
    }

    public void validate(Director director) {
        if (director.getName().isBlank()) {
            throw new ValidationException(director + " is invalid");
        }
    }
}
