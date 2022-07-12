package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController extends AbstrcatController<User> {
    @Autowired
    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        super(userService);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) throws NotFoundException {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) throws NotFoundException {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) throws NotFoundException {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable Long id,
            @PathVariable Long otherId
    ) {
        return userService.getCommonFriends(id, otherId);
    }

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
