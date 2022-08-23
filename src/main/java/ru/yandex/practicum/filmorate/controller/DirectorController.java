package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

@RestController
@RequestMapping("/directors")
public class DirectorController extends AbstractController<Director> {
    DirectorController(DirectorService directorService) {
        super(directorService);
    }
}
