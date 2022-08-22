package ru.yandex.practicum.filmorate.model;

import java.util.HashMap;
import java.util.Map;

public enum OperationType {
    ADD(1),
    UPDATE(2),
    REMOVE(3);

    private static final Map<Integer, OperationType> operationTypes = new HashMap<>();

    static {
        for (OperationType e: values()) {
            operationTypes.put(e.code, e);
        }
    }

    private final int code;

    OperationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OperationType getByCode(int code) {
        return operationTypes.get(code);
    }
}
