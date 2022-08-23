package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class RecommendationDao {
    private final JdbcTemplate jdbcTemplate;

    public RecommendationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<Long> getRecommendations(Long id) {
        Set<Long> recommendationFilmId = new HashSet<>();
        final List<Long> userFilmsIDList = new ArrayList<>(getFilmsIDList(id));
        final String sqlQueryUsersID = "SELECT ID from USERS";
        final List<Long> allUsersIDList = new ArrayList<>(jdbcTemplate.query(sqlQueryUsersID, this::getIdForUserList));
        allUsersIDList.remove(id);
        int crossListSize = 0;
        List<Long> finalUserId = new ArrayList<>();//Лист Id Пользователей с максимальным пересечением по лайкам фильмов
        for (Long userId : allUsersIDList) {     // находим пользователя с максимальным пересечением по фильмам с лаками
            if (getCrossListFilmsId(userId, userFilmsIDList).size() > crossListSize) {
                crossListSize = getCrossListFilmsId(userId, userFilmsIDList).size();
            }
        }
        for (Long userId : allUsersIDList) {             //находим список пользователей с crossListSize = максимальному
            if (getCrossListFilmsId(userId, userFilmsIDList).size() == crossListSize) {
                finalUserId.add(userId);
            }
        }
        for (Long userId : finalUserId) {                       //составляем список id фильмов, которые не пересекаются
            List<Long> excludeUserFilmsIDList = new ArrayList<>(userFilmsIDList);
            final List<Long> otherUserFilmsIDList = new ArrayList<>(getFilmsIDList(userId));
            otherUserFilmsIDList.removeAll(excludeUserFilmsIDList);
            recommendationFilmId.addAll(otherUserFilmsIDList);
        }
        log.info("Найден список рекоммендованных фильмов для пользователя {}", id);
        return recommendationFilmId;
    }

    private List<Long> getCrossListFilmsId(Long userId, List<Long> userFilmsIDList) {
        List<Long> includeUserFilmsIDList = new ArrayList<>(userFilmsIDList);
        final List<Long> otherUserFilmsIDList = new ArrayList<>(getFilmsIDList(userId));
        includeUserFilmsIDList.retainAll(otherUserFilmsIDList);
        return includeUserFilmsIDList;
    }

    private List<Long> getFilmsIDList(Long userId) {
        final String sqlQueryFilmsID = "SELECT FILM_ID from LIKES where USER_ID = ?";
        return new ArrayList<>(jdbcTemplate.query(sqlQueryFilmsID,
                this::getFilmsIdForList, userId));
    }

    private Long getFilmsIdForList(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("FILM_ID");
    }

    private Long getIdForUserList(ResultSet rs, int rowNum) throws SQLException {
        return rs.getLong("ID");
    }
}
