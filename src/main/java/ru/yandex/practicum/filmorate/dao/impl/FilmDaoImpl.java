package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmDaoImpl extends AbstractDaoImpl implements FilmDao {
    private final JdbcTemplate jdbcTemplate;

    public FilmDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film createFilm(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("name", film.getName());
        values.put("description", film.getDescription());
        values.put("release_date", film.getReleaseDate());
        values.put("duration", film.getDuration());
        values.put("mpa_id", film.getMpa().getId());
        values.put("rate", film.getRate());

        Long filmId = simpleJdbcInsert.executeAndReturnKey(values).longValue();

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> addFilmGenre(filmId, genre.getId()));
        }

        return getFilmById(filmId);
    }

    @Override
    public List<Film> getAll() {
        return jdbcTemplate.query(readSql("films_get_all"), this::parseFilm);
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(readSql("films_get_by_id"),
                    this::parseFilm, id);
            log.info("Найден фильм: {} {}", film.getId(), film.getName());
            return film;
        } catch (Exception e) {
            throw new NotFoundException("Фильм с идентификатором " + id + " не найден: " + e.getMessage());
        }
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(readSql("films_update"),
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update(readSql("films_remove_genre"), film.getId());
        if (film.getGenres() != null) {
            List<Integer> genresIds = jdbcTemplate.query(readSql("films_get_genres"),
                    this::parseGenreIds, film.getId());

            film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(id -> !genresIds.contains(id))
                    .collect(Collectors.toSet())
                    .forEach(genreId -> {
                        jdbcTemplate.update(readSql("films_add_genre"), film.getId(), genreId);
                    });
        }

        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getPopular(Long count) {
        return jdbcTemplate.query(readSql("films_get_popular"),
                this::parseFilm, count);
    }

    private void addFilmGenre(Long filmId, Integer genreId) {
        jdbcTemplate.update(readSql("films_add_genre"), filmId, genreId);
    }


    private Film parseFilm(ResultSet rs, int rowNum) throws SQLException {
        return new Film(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("release_date").toLocalDate(),
                rs.getInt("duration"),
                parseMpa(rs, rowNum),
                rs.getLong("rate"),
                parseGenre(rs.getString("genres"), rowNum)
        );
    }

    private Mpa parseMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa_name")
        );
    }

    private List<Genre> parseGenre(String data, int rowNum) {
        if (data.equals("_")) {
            return List.of();
        }
        return Arrays.stream(data.split(","))
                .map(str -> {
                    String[] params = str.split("_");
                    return new Genre(Integer.parseInt(params[0].trim()), params[1].trim());
                })
                .collect(Collectors.toList());
    }

    private Integer parseGenreIds(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("genre_id");
    }
}
