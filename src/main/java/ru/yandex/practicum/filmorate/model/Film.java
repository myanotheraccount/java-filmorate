package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.controller.ValidationException;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Slf4j
public class Film {
    private Long id;
    @NotNull
    @NotBlank
    private String name;
    @NotNull
    @Size(max=200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Positive
    private Integer duration;

    public static void validate(Film film) throws ValidationException {
        if (film.getId() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error(film + " is invalid");
            throw new ValidationException();
        }
    }
}
