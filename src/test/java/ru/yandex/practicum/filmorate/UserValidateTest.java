package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.controller.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserValidateTest {
    @Autowired
    private LocalValidatorFactoryBean validator;
    private static User user;
    private static UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        user = new User();
        user.setId(1L);
        user.setName("name");
        user.setLogin("login");
        user.setEmail("test@test.ru");
        user.setBirthday(LocalDate.of(2022, 1, 1));
    }

    @Test
    void isCorrect() {
        assertDoesNotThrow(() -> userController.validate(user));
    }

    @Test
    void invalidId() {
        user.setId(null);
        assertThrows(ValidationException.class, () -> userController.validate(user));
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
        assertThrows(ValidationException.class, () -> userController.validate(user));

        user.setLogin(null);
        assertEquals(2, validator.validateProperty(user, "login").size());
    }

    @Test
    void noName() throws ValidationException {
        user.setName(null);
        userController.validate(user);
        assertEquals(user.getName(), user.getLogin());
        assertDoesNotThrow(() -> userController.validate(user));

        user.setName("");
        userController.validate(user);
        assertEquals(user.getName(), user.getLogin());
        assertDoesNotThrow(() -> userController.validate(user));
    }

    @Test
    void invalidBirthday() {
        user.setBirthday(null);
        assertEquals(1, validator.validateProperty(user, "birthday").size());

        user.setBirthday(LocalDate.now().plusDays(1));
        assertEquals(1, validator.validateProperty(user, "birthday").size());
    }
}
