package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.LikesDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

@Component
@Slf4j
public class LikesDaoImpl extends AbstractDaoImpl implements LikesDao {
    private final JdbcTemplate jdbcTemplate;

    public LikesDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(readSql("likes_add"), filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        int removeCount = jdbcTemplate.update(readSql("likes_remove"), filmId, userId);
        if (removeCount < 1) {
            throw new NotFoundException("Не удалось удалить like");
        }
    }
}
