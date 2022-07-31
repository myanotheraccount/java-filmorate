package ru.yandex.practicum.filmorate.dao;

public interface LikesDao {
    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}
