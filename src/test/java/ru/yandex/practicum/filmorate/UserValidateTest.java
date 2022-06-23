package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
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

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("name");
        user.setLogin("login");
        user.setEmail("test@test.ru");
        user.setBirthday(LocalDate.of(2022, 1, 1));
    }

    @Test
    void isCorrect() {
        assertDoesNotThrow(() -> User.validate(user));
    }

    @Test
    void invalidId() {
        user.setId(null);
        assertThrows(ValidationException.class, () -> User.validate(user));
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
        assertThrows(ValidationException.class, () -> User.validate(user));

        user.setLogin(null);
        assertEquals(2, validator.validateProperty(user, "login").size());
    }

    @Test
    void noName() throws ValidationException {
        user.setName(null);
        User.validate(user);
        assertEquals(user.getName(), user.getLogin());
        assertDoesNotThrow(() -> User.validate(user));

        user.setName("");
        User.validate(user);
        assertEquals(user.getName(), user.getLogin());
        assertDoesNotThrow(() -> User.validate(user));
    }

    @Test
    void invalidBirthday() {
        user.setBirthday(null);
        assertEquals(1, validator.validateProperty(user, "birthday").size());

        user.setBirthday(LocalDate.now().plusDays(1));
        assertEquals(1, validator.validateProperty(user, "birthday").size());
    }
}
