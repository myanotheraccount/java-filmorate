package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.FriendsDao;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FriendsDaoImpl implements FriendsDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String FRIENDS_ADD = "INSERT INTO FRIENDSHIP(user_id, friend_id, status) VALUES (?, ?, ?);";
    private static final String FRIENDS_GET = "SELECT *\n" +
            "FROM USERS\n" +
            "WHERE ID IN (\n" +
            "    SELECT FRIEND_ID as ID\n" +
            "    FROM FRIENDSHIP\n" +
            "    WHERE USER_ID = ?);";
    private static final String FRIENDS_REMOVE = "DELETE\n" +
            "FROM FRIENDSHIP\n" +
            "WHERE (USER_ID = ? AND FRIEND_ID = ?) OR (USER_ID = ? AND FRIEND_ID = ?);";

    public FriendsDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(FRIENDS_ADD, userId, friendId, true);
        log.info("Пользователь {} добавил друга {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbcTemplate.update(FRIENDS_REMOVE, userId, friendId, friendId, userId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        List<User> users = jdbcTemplate.query(FRIENDS_GET,
                this::mapRowToUser,
                userId
        );
        log.info("Найдены друзья пользователя {}", userId);
        return users;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<Long> userFriendsIds = getFriends(id).stream()
                .map(User::getId)
                .collect(Collectors.toList());
        log.info("Найдены общие друзья пользователя {} и {}", id, otherId);
        return getFriends(otherId).stream()
                .filter(user -> userFriendsIds.contains(user.getId()))
                .collect(Collectors.toList());
    }

    private User mapRowToUser(ResultSet resultSet, int rowNum) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("login"),
                resultSet.getDate("birthdate").toLocalDate()
        );
    }
}
