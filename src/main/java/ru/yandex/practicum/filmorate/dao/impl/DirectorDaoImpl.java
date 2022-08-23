package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.DirectorDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class DirectorDaoImpl extends AbstractDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;

    public DirectorDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("name", director.getName());

        Long directorId = simpleJdbcInsert.executeAndReturnKey(values).longValue();

        log.info("Добавлен режиссер с id = {}", directorId);
        return getDirectorById(directorId);
    }

    @Override
    public Director getDirectorById(Long id) {
        try {
            Director director = jdbcTemplate.queryForObject(readSql("directors_get_by_id"),
                    this::parseDirector, id);
            log.info("Найден режиссер: {}", director.getName());
            return director;
        } catch (Exception e) {
            throw new NotFoundException(String.format("режиссер с id = %d не найден: %s", id, e.getMessage()));
        }

    }

    @Override
    public List<Director> getAll() {
        List<Director> directors = jdbcTemplate.query(readSql("directors_get_all"), this::parseDirector);
        log.info("Найден список всех режиссеров");
        return directors;
    }

    @Override
    public Director updateDirector(Director director) {
        jdbcTemplate.update(readSql("directors_update"),
                director.getName(),
                director.getId()
        );
        log.info("Обновлены данные режиссера с id = {}", director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(readSql("directors_remove_by_id"), id);
        log.info("Удален режиссер с id = {}", id);
    }

    private Director parseDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
