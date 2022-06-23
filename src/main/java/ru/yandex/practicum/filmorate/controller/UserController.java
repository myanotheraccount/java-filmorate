package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class UserController {
    private final HashMap<Long, User> users = new HashMap<>();

    @PostMapping("/users")
    public User addUser(@Valid @RequestBody User user) throws ValidationException {
        user.setId((long) (users.size() + 1));
        User.validate(user);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping("/users")
    public User updateUser(@Valid @RequestBody User user) throws ValidationException {
        User.validate(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
        } else {
            throw new ValidationException();
        }
        return user;
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return new ArrayList<User>(users.values());
    }

}
