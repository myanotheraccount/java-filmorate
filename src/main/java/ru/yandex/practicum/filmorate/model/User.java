package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.controller.ValidationException;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

@Data
@Slf4j
public class User extends DataStorage {
    private String name;
    @NotNull
    @NotBlank
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;
    @NotNull
    @Past
    private LocalDate birthday;
}
