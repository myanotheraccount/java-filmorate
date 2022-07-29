package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserDaoImpl extends AbstractDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;

    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> values = new HashMap<>();
        values.put("name", user.getName());
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("birthdate", user.getBirthday());

        return findUserById(simpleJdbcInsert.executeAndReturnKey(values).longValue());
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update(readSql("users_update"),
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId()
        );
        return findUserById(user.getId());
    }

    @Override
    public User findUserById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(readSql("users_get_by_id"),
                    this::mapRowToUser, id);
            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
            return user;
        } catch (Exception e) {
            throw new NotFoundException("Пользователь с идентификатором " + id + " не найден.");
        }
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query(readSql("users_get_all"), this::mapRowToUser);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);
        jdbcTemplate.update(readSql("users_add_friend"),
                user.getId(), friend.getId(), true);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);
        jdbcTemplate.update(readSql("users_remove_friend"),
                user.getId(), friend.getId(), friend.getId(), user.getId());
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = findUserById(userId);
        return jdbcTemplate.query(readSql("users_get_friends"),
                this::mapRowToUser,
                user.getId()
        );
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        List<Long> userFriendsIds = getFriends(id).stream()
                .map(User::getId)
                .collect(Collectors.toList());
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
