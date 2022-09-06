package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.dao.impl.FilmDaoImpl;
import ru.yandex.practicum.filmorate.dao.impl.MarksDaoImpl;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql({"/marks_data.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RecommendationsTest {
    private final FilmDaoImpl filmDao;
    private final MarksDaoImpl marksDao;

    @Test
    @DisplayName("Тест работы алгоритма рекомендаций через обратное прогнозирование оценок")
    public void testRecommendationsAlgorithm() {
        List<Film> films;
        float prognosticMark;
        float realMark;
        float errorPercent;
        for (long userId = 1; userId <= 5; userId++) {
            films = filmDao.getBackCastingRecommendations(userId);
            for (Film film : films) {
                prognosticMark = film.getRate();
                realMark = marksDao.getMark(film.getId(), userId);
                errorPercent = abs(prognosticMark - realMark) / realMark * 100;
                assertTrue(errorPercent < 5,
                        "Расхождение между прогнозируемой и реальной оценкой превышает 5%");
            }
        }
    }

    @Test
    @DisplayName("Тест значений прогнозируемых оценок на соответствие формату оценок")
    public void testRecommendationsMarksLimits() {
        List<Film> films;
        float prognosticMark;
        for (long userId = 1; userId <= 5; userId++) {
            films = filmDao.getRecommendations(userId);
            for (Film film : films) {
                prognosticMark = film.getRate();
                assertTrue(prognosticMark >= 5,
                        "В рекомендациях присутствуют фильмы с отрицательной оценкой");
                assertTrue(prognosticMark <= 10,
                        "В рекомендациях присутствуют фильмы с оценкой выше допустимого передела");
            }
        }
    }

    @ParameterizedTest(name = "{index} Проверка порядка вывода популярных фильмов, ожидается фильм {0}")
    @DisplayName("Тест порядка вывода самых популярных фильмов")
    @MethodSource("popularsOrder")
    public void testPopularsOrder(int index, long id) {
        List<Film> films = filmDao.getPopular(10L);
        assertEquals(films.get(index).getId(), id, "Нарушен ожидаемый порядок вывода популярных фильмов");
    }

    private static Stream<Arguments> popularsOrder() {
        return Stream.of(
                Arguments.of(0, 2L),
                Arguments.of(1, 4L),
                Arguments.of(2, 5L),
                Arguments.of(3, 1L),
                Arguments.of(4, 3L)
        );
    }
}