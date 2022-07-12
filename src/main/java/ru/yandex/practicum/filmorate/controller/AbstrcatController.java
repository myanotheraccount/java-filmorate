package ru.yandex.practicum.filmorate.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.DataStorage;
import ru.yandex.practicum.filmorate.service.AbstractService;

@Slf4j
public abstract class AbstrcatController<T extends DataStorage> {
    private final AbstractService<T> service;

    @Autowired
    AbstrcatController(AbstractService<T> service) {
        this.service = service;
    }

    private Long id = 0L;

    private Long generateId() {
        return ++id;
    }

    @PostMapping
    public T create(@Valid @RequestBody T item) throws ValidationException {
        item.setId(0L);
        validate(item);
        item.setId(generateId());
        service.create(item);
        return item;
    }

    @PutMapping
    public T update(@Valid @RequestBody T item) throws IOException {
        validate(item);
        service.update(item);
        return item;
    }

    @GetMapping
    public List<T> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public T get(@PathVariable("id") Long id) throws NotFoundException {
        return service.get(id);
    }

    public void validate(T item) throws ValidationException {
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(final NotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(final ValidationException e) {
        return Map.of("error", e.getMessage());
    }
}
