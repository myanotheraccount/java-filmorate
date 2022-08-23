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
public class FriendsDaoImpl extends AbstractDaoImpl implements FriendsDao {
    private final JdbcTemplate jdbcTemplate;

    public FriendsDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbcTemplate.update(readSql("friends_add"), userId, friendId, true);
        log.info("Пользователь {} добавил друга {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbcTemplate.update(readSql("friends_remove"), userId, friendId, friendId, userId);
        log.info("Пользователь {} удалил из друзей {}", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        List<User> users = jdbcTemplate.query(readSql("friends_get"),
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
