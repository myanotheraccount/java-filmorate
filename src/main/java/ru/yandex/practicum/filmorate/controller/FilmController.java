package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController extends AbstrcatController<Film> {

    @Autowired
    FilmService filmService;

    @Autowired
    public FilmController(FilmService service) {
        super(service);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) throws NotFoundException {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) throws NotFoundException {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopuolar(
            @RequestParam(defaultValue = "10") Long count
    )  {
        return filmService.getPopular(count);
    }

    public void validate(Film film) throws ValidationException {
        if (film.getId() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error(film + " is invalid");
            throw new ValidationException();
        }
    }
}
