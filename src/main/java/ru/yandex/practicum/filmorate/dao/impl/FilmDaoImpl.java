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

        log.info("Добавлен фильм: {}", filmId);
        return getFilmById(filmId);
    }

    @Override
    public List<Film> getAll() {
        List<Film> films = jdbcTemplate.query(readSql("films_get_all"), this::parseFilm);
        log.info("Найден список фильмов");
        return films;
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

        log.info("Обновлен фильм с id = {}", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getPopular(Long count) {
        List<Film> films = jdbcTemplate.query(readSql("films_get_popular"),
                this::parseFilm, count);
        log.info("Найден список популярных фильмов");
        return films;
    }

    @Override
    public List<Film> getByFilter(Long directorId, String sortBy) {
        List<Film> films = jdbcTemplate.query(readSql("films_get_by_filter"),
                this::parseFilm, directorId, sortBy, sortBy);
        log.info("Найдены фильмы по фильтру id режиссера = {}, сортировка = {}", directorId, sortBy);
        return films;
    }

    @Override
    public List<Film> getFilmsByParams(String queryText, List<String> queryParams) {
        List<Film> films;
        queryText = "%" + queryText + "%";
        int queryParamsSize = queryParams.size();
        switch (queryParamsSize) {
            case 1: {
                if (queryParams.get(0).equals("director")) {
                    films = jdbcTemplate.query(readSql("films_get_popular_by_director"),
                            this::parseFilm, queryText);
                    log.info("Выполнен поиск фильмов по режиссеру = {}", queryText);
                } else {
                    films = jdbcTemplate.query(readSql("films_get_popular_by_title"),
                            this::parseFilm, queryText);
                    log.info("Выполнен поиск фильмов по названию = {}", queryText);
                }
                break;
            }
            case 2: {
                films = jdbcTemplate.query(readSql("films_get_popular_by_director_or_title"),
                        this::parseFilm, queryText, queryText);
                log.info("Выполнен поиск фильмов по названию и режиссеру = {}", queryText);
                break;
            }
            default:
                throw new NotFoundException("Передано недопустимое количество параметра для поиска: " + queryParams.size());
        }
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbcTemplate.query(readSql("films_get_popular_common"),
                this::parseFilm, userId, friendId);
        log.info("Найдены общие фильмы у {} и {}", userId, friendId);
        return films;
    }

    @Override
    public List<Film> getPopular(long count, Optional<Integer> genreId, Optional<Integer> year) {
        List<Film> films;

        if (genreId.isPresent() && year.isPresent()) {
            films = jdbcTemplate.query(readSql("films_get_popular_filter_genre_year"),
                    this::parseFilm, genreId.get(), year.get(), count);
            log.info("Найдены популярные фильмы по жанру = {} и по году = {}", genreId, year);
        } else if (genreId.isPresent()) {
            films = jdbcTemplate.query(readSql("films_get_popular_filter_genre"),
                    this::parseFilm, genreId.get(), count);
            log.info("Найдены популярные фильмы по жанру = {}", genreId);
        } else if (year.isPresent()) {
            films = jdbcTemplate.query(readSql("films_get_popular_filter_year"),
                    this::parseFilm, year.get(), count);
            log.info("Найдены популярные фильмы по году = {}", year);
        } else {
            films = getPopular(count);
            log.info("Найдены популярные фильмы");
        }
        return films;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(readSql("films_remove_by_id"), id);
        log.info("Удален фильм с id = {}", id);
    }

    private void addFilmGenre(Long filmId, Integer genreId) {
        jdbcTemplate.update(readSql("films_add_genre"), filmId, genreId);
        log.info("Добавлен жанр = {} фильма = {}", genreId, filmId);
    }

    private void addFilmDirector(Long filmId, Long directorId) {
        jdbcTemplate.update(readSql("films_add_director"), filmId, directorId);
        log.info("Добавлен режиссер = {} фильма = {}", directorId, filmId);
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