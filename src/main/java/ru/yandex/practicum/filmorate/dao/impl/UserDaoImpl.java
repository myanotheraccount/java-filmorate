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

@Component
@Slf4j
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;
    private static final String USERS_GET_ALL = "SELECT * FROM USERS;";
    private static final String USERS_GET_BY_ID = "SELECT * FROM USERS WHERE ID = ?;";
    private static final String USERS_REMOVE_BY_ID = "DELETE FROM USERS WHERE ID = ?;";
    private static final String USERS_UPDATE = "UPDATE USERS SET NAME = ?, EMAIL = ?, LOGIN = ?, BIRTHDATE = ? WHERE ID = ?;";

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

        Long userId = simpleJdbcInsert.executeAndReturnKey(values).longValue();
        log.info("Добавлен пользователь с id = {}", userId);
        return findUserById(userId);
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update(USERS_UPDATE,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                user.getBirthday(),
                user.getId()
        );
        log.info("Обновлен пользователь с id = {}", user.getId());
        return findUserById(user.getId());
    }

    @Override
    public User findUserById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(USERS_GET_BY_ID,
                    this::mapRowToUser, id);
            log.info("Найден пользователь: {} {}", user.getId(), user.getName());
            return user;
        } catch (Exception e) {
            throw new NotFoundException("Пользователь с идентификатором " + id + " не найден.");
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = jdbcTemplate.query(USERS_GET_ALL, this::mapRowToUser);
        log.info("Найден список пользователей");
        return users;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(USERS_REMOVE_BY_ID, id);
        log.info("Удален пользователь {}", id);
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
