package ru.yandex.practicum.filmorate.controller;

import java.util.List;

import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.service.AbstractService;

@Slf4j
public abstract class AbstractController<T> {
    private final AbstractService<T> service;

    @Autowired
    AbstractController(AbstractService<T> service) {
        this.service = service;
    }

    @PostMapping
    public T create(@Valid @RequestBody T item) {
        return service.create(item);
    }

    @PutMapping
    public T update(@Valid @RequestBody T item) {
        return service.update(item);
    }

    @GetMapping
    public List<T> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public T get(@PathVariable("id") Long id) {
        return service.get(id);
    }
}
