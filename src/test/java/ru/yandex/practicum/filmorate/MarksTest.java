package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql({"/marks_data.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MarksTest {
    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest(name = "{index} Запрос оценки для фильма {0} от пользователя {1}, ожидаемое значение {2}")
    @DisplayName("Тест запроса на получение оценки фильма от конкретного пользователя")
    @MethodSource("existingMarksValues")
    void getMarkValidFilmIdUserIdTest(long filmId, long userId, String value) throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/films/" + filmId + "/mark/" + userId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(value));
    }

    @ParameterizedTest(name = "{index} Запрос несуществующей оценки для фильма {0} от пользователя {1}")
    @DisplayName("Тест запроса на получение несуществующей оценки фильма")
    @MethodSource("invalidMarkParameters")
    void getMarkInValidFilmIdUserIdTest(long filmId, long userId) throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/films/" + filmId + "/mark/" + userId))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @ParameterizedTest(name = "{index} Запрос средней оценки для фильма {0}, ожидаемое значение {1}")
    @DisplayName("Тест запроса на получение средней оценки фильма")
    @MethodSource("avgExistingMarkValues")
    void getAvgMarkValidFilmIdTest(long filmId, String value) throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/films/" + filmId + "/mark"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(value));
    }

    @ParameterizedTest(name = "{index} Запрос несуществующей средней оценки для фильма {0}")
    @DisplayName("Тест запроса на получение несуществующей средней оценки фильма")
    @MethodSource("invalidAvgMarkParameters")
    void getAvgMarkInValidFilmIdTest(long filmId) throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/films/" + filmId + "/mark"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @ParameterizedTest(name = "{index} Запрос на добавление оценки {0}, корректность значения оценки — {1}")
    @DisplayName("Тест запроса на добавление/обновление оценки с корректными и некорректными значениями оценки")
    @MethodSource("markValuesForAddMark")
    void addMarkValidInvalidValuesTest(String markValue, boolean isValid) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/films/6/mark/1/" + markValue))
                .andExpect((isValid)
                        ? MockMvcResultMatchers.status().isOk()
                        : MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("Тест запроса на добавление/обновление оценки несуществующему фильму")
    void addMarkInvalidFilmIdTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/films/-1/mark/1/5.0"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @ParameterizedTest(name = "{index} Запрос на удаление оценки для фильма {0} от пользователя {1}," +
            " корректность параметров — {2}")
    @DisplayName("Тест запроса на удаление оценки с корректными и некорректными параметрами фильма и пользователя")
    @MethodSource("markParametersRemoveMark")
    void removeMarkValidInvalidParametersTest(long filmId, long userId, boolean isValid) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .put("/films/6/mark/1/5.0"));
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/films/" + filmId + "/mark/" + userId))
                .andExpect((isValid)
                        ? MockMvcResultMatchers.status().isOk()
                        : MockMvcResultMatchers.status().isNotFound());
    }

    private static Stream<Arguments> existingMarksValues() {
        return Stream.of(
                Arguments.of(1, 1, "7.0"),
                Arguments.of(1, 2, "7.0"),
                Arguments.of(1, 5, "5.0"),
                Arguments.of(2, 2, "9.0"),
                Arguments.of(2, 3, "7.0"),
                Arguments.of(3, 1, "5.0"),
                Arguments.of(3, 3, "4.0"),
                Arguments.of(3, 5, "3.0"),
                Arguments.of(4, 1, "8.0"),
                Arguments.of(4, 5, "5.7"),
                Arguments.of(5, 4, "4.0"),
                Arguments.of(5, 5, "9.0")
        );
    }

    private static Stream<Arguments> avgExistingMarkValues() {
        return Stream.of(
                Arguments.of(1, "6.3"),
                Arguments.of(2, "8.0"),
                Arguments.of(3, "4.0"),
                Arguments.of(4, "6.9"),
                Arguments.of(5, "6.5")
        );
    }

    private static Stream<Arguments> invalidMarkParameters() {
        return Stream.of(
                Arguments.of(-1, 1),
                Arguments.of(1, -1),
                Arguments.of(-1, -1),
                Arguments.of(1, 3)
        );
    }

    private static Stream<Arguments> invalidAvgMarkParameters() {
        return Stream.of(
                Arguments.of(-1),
                Arguments.of(7)
        );
    }

    private static Stream<Arguments> markValuesForAddMark() {
        return Stream.of(
                Arguments.of("5.237", true),
                Arguments.of("1", true),
                Arguments.of("10", true),
                Arguments.of("0", false),
                Arguments.of("-1", false),
                Arguments.of("1000000", false),
                Arguments.of("0.999", false),
                Arguments.of("10.001", false)
        );
    }

    private static Stream<Arguments> markParametersRemoveMark() {
        return Stream.of(
                Arguments.of(6, 1, true),
                Arguments.of(1, 3, false),
                Arguments.of(1, -1, false),
                Arguments.of(-1, 1, false),
                Arguments.of(-1, -1, false)
        );
    }
}
