package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.NotFoundException;

import java.util.List;

@Service
public interface AbstractService<T> {
    T create(T item);

    void update(T item) throws NotFoundException;

    T get(Long id) throws NotFoundException;

    List<T> getAll();
}
