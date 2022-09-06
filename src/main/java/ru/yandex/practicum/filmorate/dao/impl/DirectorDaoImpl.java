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
public class DirectorDaoImpl implements DirectorDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String DIRECTORS_GET_BY_ID = "SELECT * FROM DIRECTORS WHERE ID = ?;";
    private static final String DIRECTORS_GET_ALL = "SELECT * FROM DIRECTORS;";
    private static final String DIRECTORS_REMOVE_BY_ID = "DELETE FROM DIRECTORS WHERE ID = ?;";
    private static final String DIRECTORS_UPDATE_BY_ID = "UPDATE DIRECTORS SET NAME = ? WHERE ID = ?;";

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
            Director director = jdbcTemplate.queryForObject(DIRECTORS_GET_BY_ID,
                    this::parseDirector, id);
            log.info("Найден режиссер: {}", director.getName());
            return director;
        } catch (Exception e) {
            throw new NotFoundException(String.format("режиссер с id = %d не найден: %s", id, e.getMessage()));
        }

    }

    @Override
    public List<Director> getAll() {
        List<Director> directors = jdbcTemplate.query(DIRECTORS_GET_ALL, this::parseDirector);
        log.info("Найден список всех режиссеров");
        return directors;
    }

    @Override
    public Director updateDirector(Director director) {
        jdbcTemplate.update(DIRECTORS_UPDATE_BY_ID,
                director.getName(),
                director.getId()
        );
        log.info("Обновлены данные режиссера с id = {}", director.getId());
        return getDirectorById(director.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(DIRECTORS_REMOVE_BY_ID, id);
        log.info("Удален режиссер с id = {}", id);
    }

    private Director parseDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
