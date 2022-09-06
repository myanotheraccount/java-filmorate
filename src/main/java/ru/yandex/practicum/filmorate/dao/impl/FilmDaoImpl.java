package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FilmDaoImpl implements FilmDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String FILMS_ADD_GENRE = "INSERT INTO FILMS_GENRES(film_id, genre_id) VALUES (?, ?)";
    private static final String FILM_ADD_DIRECTOR = "INSERT INTO FILMS_DIRECTORS(film_id, director_id) VALUES (?, ?);";
    private static final String FILM_GET_ALL = "SELECT F.ID,\n" +
            "       F.NAME,\n" +
            "       F.DESCRIPTION,\n" +
            "       F.RELEASE_DATE,\n" +
            "       F.DURATION,\n" +
            "       F.RATE,\n" +
            "       F.MPA_ID,\n" +
            "       M.MPA_NAME,\n" +
            "       STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',')    AS GENRES,\n" +
            "       STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "GROUP BY F.ID;\n";
    private static final String FILMS_GET_BACKCASTING_RECOMMENDATIONS = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                CASE\n" +
            "                    WHEN (M.PROGNOSTIC_MARK > 10) THEN 10\n" +
            "                    WHEN (M.PROGNOSTIC_MARK < 1) THEN 1\n" +
            "                    ELSE ROUND(M.PROGNOSTIC_MARK, 1)\n" +
            "                    END                                                                       AS RATE,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "         INNER JOIN\n" +
            "         (SELECT MARKS.FILM_ID,\n" +
            "                 SUM((MARK_VALUE + OPINION_DELTA) * OPINION_WEIGHT) / SUM(OPINION_WEIGHT) AS PROGNOSTIC_MARK\n" +
            "          FROM\n" +
            "              (SELECT L1.USER_ID,\n" +
            "                      AVG((l2.MARK_VALUE - l1.MARK_VALUE)) as OPINION_DELTA,\n" +
            "                      COUNT(L1.USER_ID)                    as OPINION_WEIGHT\n" +
            "               FROM LIKES AS L1\n" +
            "                        INNER JOIN LIKES AS L2 ON L2.FILM_ID = L1.FILM_ID\n" +
            "               WHERE L2.USER_ID = ?\n" +
            "                 AND L1.USER_ID != ?\n" +
            "               GROUP BY L1.USER_ID)\n" +
            "                  AS OPINIONS\n" +
            "                  INNER JOIN\n" +
            "              (SELECT L3.*\n" +
            "               FROM LIKES AS L3\n" +
            "               WHERE L3.FILM_ID IN\n" +
            "                     (SELECT DISTINCT L4.FILM_ID FROM LIKES AS L4 WHERE L4.USER_ID = ?))\n" +
            "                  AS MARKS\n" +
            "              ON OPINIONS.USER_ID = MARKS.USER_ID\n" +
            "          GROUP BY FILM_ID)\n" +
            "         AS M on F.ID = M.FILM_ID\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY F.ID;";
    private static final String FILMS_GET_BY_FILTER = "SELECT F.ID,\n" +
            "       F.NAME,\n" +
            "       F.DESCRIPTION,\n" +
            "       F.RELEASE_DATE,\n" +
            "       F.DURATION,\n" +
            "       COUNT(L.FILM_ID)                                                              AS RATE,\n" +
            "       F.MPA_ID,\n" +
            "       M.MPA_NAME,\n" +
            "       STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "       STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS_DIRECTORS AS FD\n" +
            "         LEFT JOIN FILMS F on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE FD.DIRECTOR_ID = ?\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY CASE WHEN 'year' = ? THEN F.RELEASE_DATE END,\n" +
            "         CASE WHEN 'likes' = ? THEN RATE END DESC";
    private static final String FILMS_GET_BY_ID = "SELECT F.ID,\n" +
            "       F.NAME,\n" +
            "       F.DESCRIPTION,\n" +
            "       F.RELEASE_DATE,\n" +
            "       F.DURATION,\n" +
            "       F.RATE,\n" +
            "       F.MPA_ID,\n" +
            "       M.MPA_NAME,\n" +
            "       STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',')    AS GENRES,\n" +
            "       STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE F.ID = ?\n" +
            "GROUP BY F.ID;\n";
    private static final String FILMS_GET_GENRES = "SELECT GENRE_ID FROM FILMS_GENRES WHERE FILM_ID = ?;";
    private static final String FILMS_GET_POPULAR = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC\n" +
            "LIMIT ?;";
    private static final String FILMS_GET_POPULAR_BY_DIRECTOR = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE UPPER(D.NAME) LIKE UPPER(?)\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC;";
    private static final String FILMS_GET_POPULAR_BY_DIRECTOR_OR_TITLE = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE UPPER(D.NAME) LIKE UPPER(?)\n" +
            "   OR UPPER(F.NAME) LIKE UPPER(?)\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC;";
    private static final String FILMS_GET_POPULAR_BY_TITLE = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE UPPER(F.NAME) LIKE UPPER(?)\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC;";
    private static final String FILMS_GET_POPULAR_COMMON = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE F.ID IN (SELECT L1.FILM_ID\n" +
            "               FROM LIKES AS L1\n" +
            "                        JOIN LIKES L2 on L2.FILM_ID = L1.FILM_ID\n" +
            "               WHERE L1.USER_ID = ?\n" +
            "                 AND L2.USER_ID = ?)\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC;";
    private static final String FILMS_GET_POPULAR_FILTER_GENRE = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM (SELECT DISTINCT FILMS.*\n" +
            "      FROM FILMS\n" +
            "               JOIN FILMS_GENRES on FILMS.ID = FILMS_GENRES.FILM_ID\n" +
            "      WHERE FILMS_GENRES.GENRE_ID = ?) AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC\n" +
            "LIMIT ?;";
    private static final String FILMS_GET_POPULAR_FILTER_GENRE_YEAR = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM (SELECT DISTINCT FILMS.*\n" +
            "      FROM FILMS\n" +
            "               JOIN FILMS_GENRES on FILMS.ID = FILMS_GENRES.FILM_ID\n" +
            "      WHERE FILMS_GENRES.GENRE_ID = ?\n" +
            "        AND YEAR(FILMS.RELEASE_DATE) = ?) AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC\n" +
            "LIMIT ?;";
    private static final String FILMS_GET_POPULAR_FILTER_YEAR = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                ROUND(AVG(L.MARK_VALUE), 1)                                                   AS RATE,\n" +
            "                COUNT(L.FILM_ID)                                                              AS VOTES,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN LIKES L on F.ID = L.FILM_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "WHERE YEAR(RELEASE_DATE) = ?\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC, VOTES DESC\n" +
            "LIMIT ?;";
    private static final String FILMS_GET_RECOMMENDATIONS = "SELECT DISTINCT F.ID,\n" +
            "                F.NAME,\n" +
            "                F.DESCRIPTION,\n" +
            "                F.RELEASE_DATE,\n" +
            "                F.DURATION,\n" +
            "                CASE\n" +
            "                    WHEN (M.PROGNOSTIC_MARK > 10) THEN 10\n" +
            "                    WHEN (M.PROGNOSTIC_MARK < 1) THEN 1\n" +
            "                    ELSE ROUND(M.PROGNOSTIC_MARK, 1)\n" +
            "                    END                                                                       AS RATE,\n" +
            "                F.MPA_ID,\n" +
            "                M.MPA_NAME,\n" +
            "                STRING_AGG(CONCAT(FG.GENRE_ID, '_', G.NAME), ',') over (PARTITION BY F.ID)    AS GENRES,\n" +
            "                STRING_AGG(CONCAT(FD.DIRECTOR_ID, '_', D.NAME), ',') over (PARTITION BY F.ID) AS DIRECTORS\n" +
            "FROM FILMS AS F\n" +
            "         LEFT JOIN MPAS M on M.MPA_ID = F.MPA_ID\n" +
            "         LEFT JOIN FILMS_GENRES FG on F.ID = FG.FILM_ID\n" +
            "         LEFT JOIN GENRES G on FG.GENRE_ID = G.ID\n" +
            "         LEFT JOIN FILMS_DIRECTORS FD on F.ID = FD.FILM_ID\n" +
            "         LEFT JOIN DIRECTORS D on FD.DIRECTOR_ID = D.ID\n" +
            "         INNER JOIN\n" +
            "\n" +
            "         (SELECT MARKS.FILM_ID,\n" +
            "                 SUM((MARK_VALUE + OPINION_DELTA) * OPINION_WEIGHT) / SUM(OPINION_WEIGHT) AS PROGNOSTIC_MARK\n" +
            "\n" +
            "          FROM\n" +
            "              /* Набор пользователей, ставивших оценку тем же фильмам что и запрашивающий. Каждому пользователю сопоставлена\n" +
            "              средняя разница между оценками запрашивающего и оценками пользователя, а также вес мнения пользователя,\n" +
            "              равный количеству сопоставлений. */\n" +
            "              (SELECT L1.USER_ID,\n" +
            "                      AVG((l2.MARK_VALUE - l1.MARK_VALUE)) as OPINION_DELTA,\n" +
            "                      COUNT(L1.USER_ID)                    as OPINION_WEIGHT\n" +
            "               FROM LIKES AS L1\n" +
            "                        INNER JOIN LIKES AS L2 ON L2.FILM_ID = L1.FILM_ID\n" +
            "               WHERE L2.USER_ID = ?\n" +
            "                 AND L1.USER_ID != ?\n" +
            "               GROUP BY L1.USER_ID)\n" +
            "                  AS OPINIONS\n" +
            "\n" +
            "                  INNER JOIN\n" +
            "              /* Набор оценок из таблицы LIKES для непросмотренных фильмов */\n" +
            "                  (SELECT L3.*\n" +
            "                   FROM LIKES AS L3\n" +
            "                   WHERE L3.FILM_ID NOT IN\n" +
            "                         (SELECT DISTINCT L4.FILM_ID FROM LIKES AS L4 WHERE L4.USER_ID = ?))\n" +
            "                  AS MARKS\n" +
            "              ON OPINIONS.USER_ID = MARKS.USER_ID\n" +
            "\n" +
            "          GROUP BY FILM_ID)\n" +
            "         AS M on F.ID = M.FILM_ID\n" +
            "WHERE PROGNOSTIC_MARK > 5\n" +
            "GROUP BY F.ID, FG.GENRE_ID, FD.DIRECTOR_ID\n" +
            "ORDER BY RATE DESC;";
    private static final String FILMS_REMOVE_BY_ID = "DELETE FROM FILMS WHERE ID = ?;";
    private static final String FILMS_REMOVE_DIRECTOR = "DELETE FROM FILMS_DIRECTORS WHERE FILM_ID = ?;";
    private static final String FILMS_REMOVE_GENRE = "DELETE FROM FILMS_GENRES WHERE FILM_ID = ?;";
    private static final String FILMS_UPDATE = "UPDATE FILMS\n" +
            "SET NAME = ?,\n" +
            "    DESCRIPTION = ?,\n" +
            "    RELEASE_DATE = ?,\n" +
            "    DURATION = ?,\n" +
            "    MPA_ID = ?\n" +
            "WHERE ID = ?;\n";

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
        List<Film> films = jdbcTemplate.query(FILM_GET_ALL, this::parseFilm);
        log.info("Найден список фильмов");
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(FILMS_GET_BY_ID,
                    this::parseFilm, id);
            log.info("Найден фильм: {} {}", film.getId(), film.getName());
            return film;
        } catch (Exception e) {
            throw new NotFoundException("Фильм с идентификатором " + id + " не найден: " + e.getMessage());
        }
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(FILMS_UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        jdbcTemplate.update(FILMS_REMOVE_GENRE, film.getId());
        if (film.getGenres() != null) {
            List<Integer> genresIds = jdbcTemplate.query(FILMS_GET_GENRES,
                    this::parseGenreIds, film.getId());

            film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(id -> !genresIds.contains(id))
                    .collect(Collectors.toSet())
                    .forEach(genreId -> {
                        jdbcTemplate.update(FILMS_ADD_GENRE, film.getId(), genreId);
                    });
        }

        jdbcTemplate.update(FILMS_REMOVE_DIRECTOR, film.getId());
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> addFilmDirector(film.getId(), director.getId()));
        }

        log.info("Обновлен фильм с id = {}", film.getId());
        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getPopular(Long count) {
        List<Film> films = jdbcTemplate.query(FILMS_GET_POPULAR,
                this::parseFilm, count);
        log.info("Найден список популярных фильмов");
        return films;
    }

    @Override
    public List<Film> getByFilter(Long directorId, String sortBy) {
        List<Film> films = jdbcTemplate.query(FILMS_GET_BY_FILTER,
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
                    films = jdbcTemplate.query(FILMS_GET_POPULAR_BY_DIRECTOR,
                            this::parseFilm, queryText);
                    log.info("Выполнен поиск фильмов по режиссеру = {}", queryText);
                } else {
                    films = jdbcTemplate.query(FILMS_GET_POPULAR_BY_TITLE,
                            this::parseFilm, queryText);
                    log.info("Выполнен поиск фильмов по названию = {}", queryText);
                }
                break;
            }
            case 2: {
                films = jdbcTemplate.query(FILMS_GET_POPULAR_BY_DIRECTOR_OR_TITLE,
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
        List<Film> films = jdbcTemplate.query(FILMS_GET_POPULAR_COMMON,
                this::parseFilm, userId, friendId);
        log.info("Найдены общие фильмы у {} и {}", userId, friendId);
        return films;
    }

    @Override
    public List<Film> getPopular(long count, Optional<Integer> genreId, Optional<Integer> year) {
        List<Film> films;

        if (genreId.isPresent() && year.isPresent()) {
            films = jdbcTemplate.query(FILMS_GET_POPULAR_FILTER_GENRE_YEAR,
                    this::parseFilm, genreId.get(), year.get(), count);
            log.info("Найдены популярные фильмы по жанру = {} и по году = {}", genreId, year);
        } else if (genreId.isPresent()) {
            films = jdbcTemplate.query(FILMS_GET_POPULAR_FILTER_GENRE,
                    this::parseFilm, genreId.get(), count);
            log.info("Найдены популярные фильмы по жанру = {}", genreId);
        } else if (year.isPresent()) {
            films = jdbcTemplate.query(FILMS_GET_POPULAR_FILTER_YEAR,
                    this::parseFilm, year.get(), count);
            log.info("Найдены популярные фильмы по году = {}", year);
        } else {
            films = getPopular(count);
            log.info("Найдены популярные фильмы");
        }
        return films;
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        List<Film> films = jdbcTemplate.query(FILMS_GET_RECOMMENDATIONS,
                this::parseFilm, userId, userId, userId);
        log.info("Составлен список рекомендованных фильмов для пользователя {}.", userId);
        return films;
    }

    /*
    Метод использует тот же алгоритм в запросе, что и getRecommendations, но выдаёт обратный прогноз оценки для уже
    просмотренных пользователем фильмов. Используется для тестирования.
     */
    public List<Film> getBackCastingRecommendations(Long userId) {
        return jdbcTemplate.query(FILMS_GET_BACKCASTING_RECOMMENDATIONS,
                this::parseFilm, userId, userId, userId);
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(FILMS_REMOVE_BY_ID, id);
        log.info("Удален фильм с id = {}", id);
    }

    private void addFilmGenre(Long filmId, Integer genreId) {
        jdbcTemplate.update(FILMS_ADD_GENRE, filmId, genreId);
        log.info("Добавлен жанр = {} фильма = {}", genreId, filmId);
    }

    private void addFilmDirector(Long filmId, Long directorId) {
        jdbcTemplate.update(FILM_ADD_DIRECTOR, filmId, directorId);
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
                rs.getFloat("rate"),
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