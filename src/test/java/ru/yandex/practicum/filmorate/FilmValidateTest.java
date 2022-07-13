package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class FilmValidateTest {
    @Autowired
    private LocalValidatorFactoryBean validator;
    private static Film film;
    private static FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage());
        film = new Film();
        film.setId(1L);
        film.setName("name");
        film.setDescription("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type a");
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        film.setDuration(120345);
    }

    @Test
    void isCorrect() {
        assertDoesNotThrow(() -> filmService.validate(film));
    }

    @Test
    void invalidId() {
        film.setId(null);
        assertThrows(ValidationException.class, () -> filmService.validate(film));
    }

    @Test
    void invalidName() {
        film.setName("");
        assertEquals(1, validator.validateProperty(film, "name").size());

        film.setName(null);
        assertEquals(2, validator.validateProperty(film, "name").size());
    }

    @Test
    void invalidDescription() {
        film.setDescription(film.getDescription() + "b");
        assertEquals(film.getDescription().length(), 201);
        assertEquals(1, validator.validateProperty(film, "description").size());

        film.setDescription(null);
        assertEquals(1, validator.validateProperty(film, "description").size());
    }

    @Test
    void invalidReleaseDate() {
        film.setReleaseDate(film.getReleaseDate().minusDays(1));
        assertThrows(ValidationException.class, () -> filmService.validate(film));

        film.setReleaseDate(null);
        assertEquals(1, validator.validateProperty(film, "releaseDate").size());
    }

    @Test
    void invalidDuration() {
        film.setDuration(-1);
        assertEquals(1, validator.validateProperty(film, "duration").size());

        film.setDuration(null);
        assertEquals(1, validator.validateProperty(film, "duration").size());
    }
}
