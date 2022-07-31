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
public class MpaDaoImpl extends AbstractDaoImpl implements MpaDao {

    JdbcTemplate jdbcTemplate;

    public MpaDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpa(Long mpaId) {
        try {
            return jdbcTemplate.queryForObject(readSql("mpa_get_by_id"), this::parseMpa, mpaId);
        } catch (Exception e) {
            throw new NotFoundException("Рейтинг фильма с id = " + mpaId + " не найден: " + e.getMessage());
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        return jdbcTemplate.query(readSql("mpa_get_all"), this::parseMpa);
    }

    private Mpa parseMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name")
        );
    }
}
