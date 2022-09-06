package ru.yandex.practicum.filmorate.dao.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.EventDao;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class EventDaoImpl implements EventDao {

    private final JdbcTemplate jdbcTemplate;
    private static final String EVENTS_ADD = "INSERT INTO EVENTS(USER_ID, ENTITY_ID, EVENT_TYPE, OPERATION_TYPE) VALUES (?, ?, ?, ?);";
    private static final String EVENTS_GET_BY_USER = "SELECT * FROM EVENTS WHERE USER_ID = ?;";

    public EventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(Event event) {
        jdbcTemplate.update(EVENTS_ADD,
                event.getUserId(),
                event.getEntityId(),
                event.getEventType().getCode(),
                event.getOperation().getCode()
        );
        log.info("Добавлено событие {} {}.", event.getOperation(), event.getEventType());
    }

    @Override
    public List<Event> getEvents(long userId) {
        List<Event> eventList = jdbcTemplate.query(EVENTS_GET_BY_USER, this::parseEvent, userId);
        log.info("Найден полный список событий");
        return eventList;
    }

    private Event parseEvent(ResultSet rs, int rowNum) throws SQLException {
        return new Event(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("entity_id"),
                EventType.getByCode(rs.getInt("event_type")),
                OperationType.getByCode(rs.getInt("operation_type")),
                rs.getTimestamp("time_stamp").getTime()
        );
    }
}
