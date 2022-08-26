package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MarksDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.util.Optional;

@Service
@Slf4j
public class MarksService {
    private final MarksDao marksDao;

    @Autowired
    public MarksService(MarksDao marksDao) {
        this.marksDao = marksDao;
    }

    public float getMark(long filmId, Optional<Long> userId) {
        return userId.map(aLong -> getMarkByUser(filmId, aLong)).orElseGet(() -> getAverageMark(filmId));
    }

    private float getMarkByUser(long filmId, long userId) {
        float mark;
        mark = marksDao.getMark(filmId, userId);
        if (mark != 0) {
            log.info("Найдена оценка фильма {} пользователем {}.", filmId, userId);
            return mark;
        } else {
            throw new NotFoundException(
                    String.format("Оценка фильма %d пользователем %d не найдена.", filmId, userId));
        }
    }

    private float getAverageMark(long filmId) {
        float mark;
        mark = marksDao.getMark(filmId);
        if (mark != 0) {
            log.info("Возвращена средняя оценка фильма {} — {}.", filmId, mark);
            return mark;
        } else {
            throw new NotFoundException(
                    String.format("Отсутствуют оценки пользователями фильма %d.", filmId));
        }
    }

    public void addMark(long filmId, long userId, float mark) {
        validate(mark);
        if (marksDao.addMark(filmId, userId, mark)) {
            log.info("Пользователь {} поставил фильму {} оценку {}.", userId, filmId, mark);
        } else {
            throw new NotFoundException(
                    String.format("Не удалось добавить оценку, возможно фильм %d или пользователь %d не существуют.",
                            filmId, userId));
        }
    }

    public void removeMark(long filmId, long userId) {
        if (marksDao.removeMark(filmId, userId)) {
            log.info("Пользователь {} удалил оценку фильму {}.", userId, filmId);
        } else {
            throw new NotFoundException(
                    String.format("Не удалось удалить оценку, возможно фильм %d, пользователь %d или сама оценка " +
                                    "не существуют.",
                            filmId, userId));
        }
    }

    public void validate(float mark) {
        if (mark < 1 || mark > 10) {
            throw new ValidationException("Задана оценка вне диапазона от 1 до 10.");
        }
    }
}
