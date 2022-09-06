package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.GenreDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class GenreDaoImpl implements GenreDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String GENRES_GET_ALL = "SELECT * FROM GENRES;";
    private static final String GENRES_GET_BY_ID = "SELECT * FROM GENRES WHERE ID = ?;";

    public GenreDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genre getGenre(Long genreId) {
        try {
            Genre genre = jdbcTemplate.queryForObject(GENRES_GET_BY_ID, this::parseGenre, genreId);
            log.info("Найден жанр по id = {}", genreId);
            return genre;
        } catch (Exception e) {
            throw new NotFoundException("Жанр фильма с id = " + genreId + " не найден: " + e.getMessage());
        }
    }

    @Override
    public List<Genre> getAllGenres() {
        List<Genre> genres = jdbcTemplate.query(GENRES_GET_ALL, this::parseGenre);
        log.info("Найден список жанров");
        return genres;
    }

    private Genre parseGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
