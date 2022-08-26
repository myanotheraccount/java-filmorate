package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MarksDao;
import ru.yandex.practicum.filmorate.model.Mark;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MarksDaoImpl extends AbstractDaoImpl implements MarksDao {
    private final JdbcTemplate jdbcTemplate;

    public MarksDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public float getMark(long filmId, long userId) {
        try {
            //noinspection ConstantConditions
            return jdbcTemplate.queryForObject(readSql("marks_get_by_film_user"), this::parseMark,
                    filmId, userId);
        } catch (Exception e) {
            return Mark.NULL_MARK;
        }
    }

    @Override
    public float getMark(long filmId) {
        try {
            //noinspection ConstantConditions
            return jdbcTemplate.queryForObject(readSql("marks_get_avg_by_film"), this::parseMark, filmId);
        } catch (Exception e) {
            return Mark.NULL_MARK;
        }
    }

    @Override
    public boolean addMark(long filmId, long userId, float mark) {
        try {
            return 0 < jdbcTemplate.update(readSql("marks_add"), filmId, userId, mark);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeMark(long filmId, long userId) {
        return 0 < jdbcTemplate.update(readSql("likes_remove"), filmId, userId);
    }

    private float parseMark(ResultSet rs, int rowNum) throws SQLException {
        return rs.getFloat("mark_value");
    }
}
