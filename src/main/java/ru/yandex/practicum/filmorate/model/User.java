package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.controller.ValidationException;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Objects;

@Data
@Slf4j
public class User {
    private String name;
    private Long id;
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

    public static void validate(User user) throws ValidationException {
        if ((user.getId() == null || user.getLogin().contains(" "))) {
            log.error(user + "is invalid");
            throw new ValidationException();
        }

        if (user.getName() == null || Objects.equals(user.getName(), "")) {
            user.setName(user.getLogin());
        }
    }
}
