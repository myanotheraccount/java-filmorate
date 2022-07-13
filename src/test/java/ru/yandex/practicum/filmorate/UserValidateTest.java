package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.Valid;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserValidateTest {
    @Autowired
    private LocalValidatorFactoryBean validator;
    private static User user;
    private static UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());
        user = new User();
        user.setId(1L);
        user.setName("name");
        user.setLogin("login");
        user.setEmail("test@test.ru");
        user.setBirthday(LocalDate.of(2022, 1, 1));
    }

    @Test
    void isCorrect() {
        assertDoesNotThrow(() -> userService.validate(user));
    }

    @Test
    void invalidId() {
        user.setId(null);
        assertThrows(ValidationException.class, () -> userService.validate(user));
    }

    @Test
    @Valid
    void invalidEmail() {
        user.setEmail("test");
        assertEquals(1, validator.validateProperty(user, "email").size());

        user.setEmail(null);
        assertEquals(2, validator.validateProperty(user, "email").size());

        user.setEmail("");
        assertEquals(1, validator.validateProperty(user, "email").size());
    }

    @Test
    void invalidLogin() {
        user.setLogin("");
        assertEquals(1, validator.validateProperty(user, "login").size());

        user.setLogin("lorem impsum");
        assertThrows(ValidationException.class, () -> userService.validate(user));

        user.setLogin(null);
        assertEquals(2, validator.validateProperty(user, "login").size());
    }

    @Test
    void noName() {
        user.setName(null);
        userService.validate(user);
        assertEquals(user.getName(), user.getLogin());
        assertDoesNotThrow(() -> userService.validate(user));

        user.setName("");
        userService.validate(user);
        assertEquals(user.getName(), user.getLogin());
        assertDoesNotThrow(() -> userService.validate(user));
    }

    @Test
    void invalidBirthday() {
        user.setBirthday(null);
        assertEquals(1, validator.validateProperty(user, "birthday").size());

        user.setBirthday(LocalDate.now().plusDays(1));
        assertEquals(1, validator.validateProperty(user, "birthday").size());
    }
}
