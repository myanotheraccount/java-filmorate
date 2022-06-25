package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController extends AbstrcatController<User> {

    @Override
    public void validate(User user) throws ValidationException {
        if ((user.getId() == null || user.getLogin().contains(" "))) {
            log.error(user + "is invalid");
            throw new ValidationException();
        }

        if (user.getName() == null || Objects.equals(user.getName(), "")) {
            user.setName(user.getLogin());
        }
    }
}
