package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AbstractService<T> {
    T create(T item);

    T update(T item);

    T get(Long id);

    List<T> getAll();

    void delete(Long id);
}
