package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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

        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> addFilmDirector(filmId, director.getId()));
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

        jdbcTemplate.update(readSql("films_remove_director"), film.getId());
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> addFilmDirector(film.getId(), director.getId()));
        }

        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getPopular(Long count) {
        return jdbcTemplate.query(readSql("films_get_popular"),
                this::parseFilm, count);
    }

    @Override
    public List<Film> getByFilter(Long directorId, String sortBy) {
        return jdbcTemplate.query(readSql("films_get_by_filter"),
                this::parseFilm, directorId, sortBy, sortBy);
    }

    @Override
    public List<Film> getFilmsByParams(String queryText, List<String> queryParams) {
        queryText = "%" + queryText + "%";
        int queryParamsSize = queryParams.size();
        switch (queryParamsSize) {
            case 1: {
                if (queryParams.get(0).equals("director")) {
                    return jdbcTemplate.query(readSql("films_get_popular_by_director"),
                            this::parseFilm, queryText);
                } else {
                    return jdbcTemplate.query(readSql("films_get_popular_by_title"),
                            this::parseFilm, queryText);
                }

            }
            case 2: {
                return jdbcTemplate.query(readSql("films_get_popular_by_director_or_title"),
                        this::parseFilm, queryText, queryText);
            }
            default:
                throw new NotFoundException("Передано недопустимое количество параметра для поиска: " + queryParams.size());
        }
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbcTemplate.query(readSql("films_get_popular_common"),
                this::parseFilm, userId, friendId);
    }

    @Override
    public List<Film> getPopular(long count, Optional<Integer> genreId, Optional<Integer> year) {

        if (genreId.isPresent() && year.isPresent()) {
            return jdbcTemplate.query(readSql("films_get_popular_filter_genre_year"),
                    this::parseFilm, genreId.get(), year.get(), count);
        } else if (genreId.isPresent()) {
            return jdbcTemplate.query(readSql("films_get_popular_filter_genre"),
                    this::parseFilm, genreId.get(), count);
        } else if (year.isPresent()) {
            return jdbcTemplate.query(readSql("films_get_popular_filter_year"),
                    this::parseFilm, year.get(), count);
        } else {
            return getPopular(count);
        }
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(readSql("films_remove_by_id"), id);
    }

    private void addFilmGenre(Long filmId, Integer genreId) {
        jdbcTemplate.update(readSql("films_add_genre"), filmId, genreId);
    }

    private void addFilmDirector(Long filmId, Long directorId) {
        jdbcTemplate.update(readSql("films_add_director"), filmId, directorId);
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
                parseGenre(rs.getString("genres"), rowNum),
                parseDirector(rs.getString("directors"), rowNum)
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

        return Arrays.stream(data.split(",")).map(str -> {
                    String[] params = str.split("_");
                    return new Genre(Integer.parseInt(params[0].trim()), params[1].trim());
                })
                .collect(Collectors.toList());
    }

    private Integer parseGenreIds(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt("genre_id");
    }

    private List<Director> parseDirector(String data, int rowNum) {
        try {
            return Arrays.stream(data.split(","))
                    .map(str -> {
                        String[] params = str.split("_");
                        return new Director(Long.parseLong(params[0].trim()), params[1].trim());
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }
}