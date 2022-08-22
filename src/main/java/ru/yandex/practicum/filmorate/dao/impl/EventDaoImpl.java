package ru.yandex.practicum.filmorate.dao.impl;

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
public class EventDaoImpl extends AbstractDaoImpl implements EventDao {

    private final JdbcTemplate jdbcTemplate;

    public EventDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(Event event) {
        jdbcTemplate.update(readSql("events_add"),
                event.getUserId(),
                event.getEntityId(),
                event.getEventType().getCode(),
                event.getOperation().getCode()
        );
    }

    @Override
    public List<Event> getEvents(long userId) {
        return jdbcTemplate.query(readSql("events_get_by_user"), this::parseEvent, userId);
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
