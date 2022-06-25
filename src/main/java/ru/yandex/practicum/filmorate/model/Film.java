package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.controller.ValidationException;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Slf4j
public class Film extends DataStorage {
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
}
