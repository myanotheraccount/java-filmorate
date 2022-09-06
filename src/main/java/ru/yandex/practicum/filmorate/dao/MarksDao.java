package ru.yandex.practicum.filmorate.dao;

public interface MarksDao {
    float getMark(long filmId, long userId);

    float getMark(long filmId);

    boolean addMark(long filmId, long userId, float mark);

    boolean removeMark(long filmId, long userId);
}
