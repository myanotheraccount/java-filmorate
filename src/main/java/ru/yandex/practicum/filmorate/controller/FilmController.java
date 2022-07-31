package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.LikesService;

import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController extends AbstractController<Film> {

    private final FilmService filmService;
    private final LikesService likesService;

    @Autowired
    public FilmController(FilmService filmService, LikesService likesService) {
        super(filmService);
        this.filmService = filmService;
        this.likesService = likesService;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        likesService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        likesService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopuolar(@RequestParam(defaultValue = "10") Long count) {
        return filmService.getPopular(count);
    }
}
