package ru.yandex.practicum.filmorate.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import ru.yandex.practicum.filmorate.model.DataStorage;

@Slf4j
public abstract class AbstrcatController<T extends DataStorage> {
    private final HashMap<Long, T> dataStorage = new HashMap<>();

    private Long id = 0L;

    private Long generateId() {
        return ++id;
    }

    @PostMapping
    public T create(@Valid @RequestBody T item) throws ValidationException {
        item.setId(generateId());
        validate(item);
        dataStorage.put(item.getId(), item);
        return item;
    }

    @PutMapping
    public T update(@Valid @RequestBody T item) throws IOException {
        validate(item);
        if (dataStorage.containsKey(item.getId())) {
            dataStorage.put(item.getId(), item);
        } else {
            log.error(item + " id not found");
            throw new IOException();
        }
        return item;
    }

    @GetMapping
    public List<T> get() {
        return new ArrayList<T>(dataStorage.values());
    }

    public void validate(T item) throws ValidationException {
    }

}
