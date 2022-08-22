package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private long eventId;
    private long userId;
    private long entityId;
    private EventType eventType;
    private OperationType operation;
    private long timestamp;

    public Event(long userId, long entityId, EventType eventType, OperationType operation) {
        this.userId = userId;
        this.entityId = entityId;
        this.eventType = eventType;
        this.operation = operation;
    }
}
