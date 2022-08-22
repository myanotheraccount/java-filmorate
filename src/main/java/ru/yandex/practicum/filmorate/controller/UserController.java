package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FriendsService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController extends AbstractController<User> {
    private final FriendsService friendsService;
    private final EventService eventService;

    @Autowired
    public UserController(UserService userService, FriendsService friendsService, EventService eventService) {
        super(userService);
        this.friendsService = friendsService;
        this.eventService = eventService;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {
        friendsService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(
            @PathVariable Long id,
            @PathVariable Long friendId
    ) {
        friendsService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        try {
            service.get(id);
            return friendsService.getFriends(id);
        } catch (Exception e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(
            @PathVariable Long id,
            @PathVariable Long otherId
    ) {
        return friendsService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getEvents(@PathVariable Long id) {
        return eventService.getEvents(id);
    }
}
