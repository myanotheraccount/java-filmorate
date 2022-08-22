package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.LikesService;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
public class FilmController extends AbstractController<Film> {

    private final FilmService filmService;
    private final LikesService likesService;
    private final DirectorService directorService;

    @Autowired
    public FilmController(FilmService filmService, LikesService likesService, DirectorService directorService) {
        super(filmService);
        this.filmService = filmService;
        this.likesService = likesService;
        this.directorService = directorService;
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
    public List<Film> getPopular(
            @RequestParam(defaultValue = "10") long count,
            @RequestParam Optional<Integer> genreId,
            @RequestParam Optional<Integer> year) {
        return filmService.getPopular(count, genreId, year);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getByFilter(
            @PathVariable Long directorId,
            @RequestParam String sortBy
    ) {
        try {
            directorService.get(directorId);
            return filmService.getByFilter(directorId, sortBy);
        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @GetMapping("/search")
    public List<Film> search(
            @RequestParam String query,
            @RequestParam @NotNull List<String> by
    ) {
        try {
            return filmService.getFilmsByParams(query, by);
        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @GetMapping("/common")
    public List<Film> getCommon(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long friendId
    ) {
        try {
            return filmService.getCommonFilms(userId, friendId);
        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }
}