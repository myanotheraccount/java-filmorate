package ru.yandex.practicum.filmorate.model;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
    LIKE(1),
    FRIEND(2),
    REVIEW(3);

    private static final Map<Integer, EventType> eventTypes = new HashMap<>();

    static {
        for (EventType e: values()) {
            eventTypes.put(e.code, e);
        }
    }

    private final int code;

    EventType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static EventType getByCode(int code) {
        return eventTypes.get(code);
    }
}
