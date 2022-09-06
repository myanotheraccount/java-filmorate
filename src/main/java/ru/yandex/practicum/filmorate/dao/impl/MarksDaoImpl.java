package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MarksDao;
import ru.yandex.practicum.filmorate.model.Mark;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MarksDaoImpl implements MarksDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String LIKES_REMOVE = "DELETE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?;";
    private static final String MARKS_ADD = "MERGE INTO LIKES(FILM_ID, USER_ID, MARK_VALUE) VALUES (?, ?, ?);";
    private static final String MARKS_GET_AVG_BY_FILM = "SELECT ROUND(AVG(MARK_VALUE), 1) AS MARK_VALUE FROM LIKES WHERE FILM_ID = ?;";
    private static final String MARKS_GET_BY_FILM_USER = "SELECT ROUND(MARK_VALUE, 1) AS MARK_VALUE FROM LIKES WHERE FILM_ID = ? AND USER_ID = ?;";

    public MarksDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public float getMark(long filmId, long userId) {
        try {
            //noinspection ConstantConditions
            return jdbcTemplate.queryForObject(MARKS_GET_BY_FILM_USER, this::parseMark,
                    filmId, userId);
        } catch (Exception e) {
            return Mark.NULL_MARK;
        }
    }

    @Override
    public float getMark(long filmId) {
        try {
            //noinspection ConstantConditions
            return jdbcTemplate.queryForObject(MARKS_GET_AVG_BY_FILM, this::parseMark, filmId);
        } catch (Exception e) {
            return Mark.NULL_MARK;
        }
    }

    @Override
    public boolean addMark(long filmId, long userId, float mark) {
        try {
            return 0 < jdbcTemplate.update(MARKS_ADD, filmId, userId, mark);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean removeMark(long filmId, long userId) {
        return 0 < jdbcTemplate.update(LIKES_REMOVE, filmId, userId);
    }

    private float parseMark(ResultSet rs, int rowNum) throws SQLException {
        return rs.getFloat("mark_value");
    }
}
