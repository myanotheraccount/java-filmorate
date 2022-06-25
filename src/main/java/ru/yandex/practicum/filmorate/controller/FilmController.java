package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController extends AbstrcatController<Film> {

    public void validate(Film film) throws ValidationException {
        if (film.getId() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error(film + " is invalid");
            throw new ValidationException();
        }
    }
}
