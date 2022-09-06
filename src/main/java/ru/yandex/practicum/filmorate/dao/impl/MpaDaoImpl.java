package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.MpaDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class MpaDaoImpl implements MpaDao {

    private final JdbcTemplate jdbcTemplate;
    private static final String MPA_GET_ALL = "SELECT * FROM MPAS;";
    private static final String MPA_GET_BY_ID = "SELECT * FROM MPAS WHERE MPA_ID = ?;";
    public MpaDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpa(Long mpaId) {
        try {
            Mpa mpa = jdbcTemplate.queryForObject(MPA_GET_BY_ID, this::parseMpa, mpaId);
            log.info("Найден рейтинг фильма с id = {}", mpaId);
            return mpa;
        } catch (Exception e) {
            throw new NotFoundException("Рейтинг фильма с id = " + mpaId + " не найден: " + e.getMessage());
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        List<Mpa> mpas = jdbcTemplate.query(MPA_GET_ALL, this::parseMpa);
        log.info("Найден список рейтингов фильмов");
        return mpas;
    }

    private Mpa parseMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name")
        );
    }
}
